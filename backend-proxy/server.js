const express = require('express');
const axios = require('axios');
const cors = require('cors');
const morgan = require('morgan');

const app = express();
const PORT = process.env.PORT || 3000;
const SICENET_URL = 'https://sicenet.itsur.edu.mx/ws/wsalumnos.asmx';

app.use(cors({
    origin: '*',
    methods: ['GET', 'POST', 'OPTIONS'],
    allowedHeaders: ['Content-Type', 'SOAPAction', 'X-Proxy-Cookie', 'Cookie'],
    exposedHeaders: ['X-Proxy-Set-Cookie']
}));

app.use(morgan('dev'));

// Forzamos la lectura del cuerpo como texto plano para evitar interferencias
app.use(express.text({ type: '*/*', limit: '10mb' }));

app.post('/soap', async (req, res) => {
    // Capturamos el SOAPAction con soporte para diversas capitalizaciones
    const soapAction = req.headers['soapaction'] || req.headers['SOAPAction'];
    const proxyCookie = req.headers['x-proxy-cookie'];

    console.log(`\n[Proxy] === Petición Recibida ===`);
    console.log(`[Proxy] SOAPAction: ${soapAction}`);

    if (!req.body || req.body.trim().length < 10) {
        console.error('[Proxy] Error: Cuerpo XML vacío o inválido');
        return res.status(400).send('Cuerpo XML inválido');
    }

    try {
        const headers = {
            'Content-Type': 'text/xml; charset=utf-8',
            'SOAPAction': soapAction,
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x86) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36'
        };

        if (proxyCookie) {
            headers['Cookie'] = proxyCookie;
            console.log(`[Proxy] Relay Cookie: ${proxyCookie.substring(0, 20)}...`);
        }

        const response = await axios({
            method: 'post',
            url: SICENET_URL,
            data: req.body.trim(),
            headers: headers,
            responseType: 'text',
            validateStatus: () => true // Transferimos cualquier código de error del servidor original
        });

        console.log(`[Proxy] Respuesta de SICENET: ${response.status}`);

        if (response.headers['set-cookie']) {
            res.setHeader('X-Proxy-Set-Cookie', JSON.stringify(response.headers['set-cookie']));
            console.log(`[Proxy] Relay Set-Cookie detectado`);
        }

        res.set('Content-Type', 'text/xml');
        res.status(response.status).send(response.data);
    } catch (error) {
        console.error('[Proxy Critical Error]', error.message);
        res.status(500).send(`Proxy Error: ${error.message}`);
    }
});

app.listen(PORT, () => {
    console.log(`=============================================`);
    console.log(`🚀 SICENET Proxy Activo en puerto ${PORT}`);
    console.log(`🔗 Redirigiendo a: ${SICENET_URL}`);
    console.log(`=============================================`);
});
