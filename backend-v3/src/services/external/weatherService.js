// ============================================================
// src/services/external/weatherService.js
// Route weather alerts via OpenWeatherMap API
// ============================================================

'use strict';

const axios = require('axios');
const { env } = require('../../config/env');
const { createLogger } = require('../../utils/logger');
const log = createLogger('weatherService');

const BASE_URL = 'https://api.openweathermap.org/data/2.5';

// Get weather for a city
async function getWeather(city) {
    if (!env.WEATHER_API_KEY) {
        log.warn('Weather API not configured');
        return null;
    }

    try {
        const response = await axios.get(`${BASE_URL}/weather`, {
            params: {
                q:     `${city},IN`,
                appid: env.WEATHER_API_KEY,
                units: 'metric',
                lang:  'en',
            },
            timeout: 5000,
        });

        const data = response.data;
        return {
            city:        data.name,
            temp:        Math.round(data.main.temp),
            feels_like:  Math.round(data.main.feels_like),
            condition:   data.weather[0].main,
            description: data.weather[0].description,
            humidity:    data.main.humidity,
            wind_speed:  data.wind.speed,
            visibility:  data.visibility / 1000,   // km
            alert:       getAlert(data.weather[0].main, data.wind.speed),
        };

    } catch (err) {
        log.error(`getWeather error for ${city}: ${err.message}`);
        return null;
    }
}

// Get route weather (origin + destination)
async function getRouteWeather(origin, destination) {
    const [from, to] = await Promise.all([
        getWeather(origin),
        getWeather(destination),
    ]);

    return { from, to };
}

// Generate driving alert
function getAlert(condition, windSpeed) {
    const dangerous = ['Thunderstorm', 'Tornado', 'Hurricane'];
    const caution   = ['Rain', 'Drizzle', 'Fog', 'Mist', 'Haze', 'Snow'];

    if (dangerous.includes(condition))  return 'danger';
    if (caution.includes(condition))    return 'caution';
    if (windSpeed > 50)                 return 'caution';
    return 'clear';
}

// Format weather message for WhatsApp
function formatWeatherMessage(origin, destination, weather) {
    const { from, to } = weather;
    const alertEmoji = { danger: '🔴', caution: '🟡', clear: '✅' };

    let msg = `🌤️ Route Weather\n\n`;

    if (from) {
        const e = alertEmoji[from.alert] || '✅';
        msg += `📍 ${from.city} (Origin)\n`;
        msg += `${e} ${from.condition} | ${from.temp}°C\n`;
        msg += `💨 Wind: ${from.wind_speed} km/h | 👁️ ${from.visibility}km\n\n`;
    }

    if (to) {
        const e = alertEmoji[to.alert] || '✅';
        msg += `📍 ${to.city} (Destination)\n`;
        msg += `${e} ${to.condition} | ${to.temp}°C\n`;
        msg += `💨 Wind: ${to.wind_speed} km/h | 👁️ ${to.visibility}km\n\n`;
    }

    const anyDanger = [from?.alert, to?.alert].includes('danger');
    const anyCaution = [from?.alert, to?.alert].includes('caution');

    if (anyDanger)       msg += `⚠️ Kharab mausam — savdhani se chalao!`;
    else if (anyCaution) msg += `🟡 Thodi savdhani zaroori hai`;
    else                 msg += `✅ Mausam theek hai — safe drive karo!`;

    return msg;
}

module.exports = { getWeather, getRouteWeather, formatWeatherMessage };
