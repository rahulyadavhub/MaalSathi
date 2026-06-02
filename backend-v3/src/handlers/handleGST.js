// ============================================================
// src/handlers/handleGST.js
// GST calculator + PDF invoice generate
// ============================================================

'use strict';

const { calculate, formatGSTMessage } = require('../services/gst/gstCalculator');
const { generateInvoice }             = require('../services/pdf/invoiceGenerator');
const { createLogger }                = require('../utils/logger');
const log = createLogger('handleGST');

async function handleGST(user, message) {
    try {
        const text = (message || '').toLowerCase().trim();

        // Invoice generate
        if (text.includes('invoice') || text.includes('bill')) {
            return getInvoiceGuide();
        }

        // GST calculation
        const amountMatch = message.match(/\d{3,}/);
        if (amountMatch) {
            const amount = parseInt(amountMatch[0]);

            // Detect rate from message
            const rate = text.includes('12') ? 0.12 : 0.05;

            const calc = calculate(amount, rate);
            return formatGSTMessage(calc);
        }

        return getGSTMenu();

    } catch (err) {
        log.error(`handleGST error: ${err.message}`);
        return `GST calculate nahi hua — dobara try karo`;
    }
}

function getGSTMenu() {
    return (
        `💰 GST Calculator\n\n` +
        `Amount type karo:\n` +
        `"GST 28000" → 5% calculate karega\n` +
        `"GST 28000 12%" → 12% calculate karega\n\n` +
        `Invoice ke liye:\n` +
        `"invoice banao"\n\n` +
        `GTA GST Rates:\n` +
        `• 5% — No ITC (most common)\n` +
        `• 12% — With ITC`
    );
}

function getInvoiceGuide() {
    return (
        `📄 GST Invoice\n\n` +
        `Invoice generate karne ke liye:\n` +
        `Active trip complete karo\n` +
        `Phir "invoice bhejo" likho\n\n` +
        `PDF WhatsApp pe aa jayega ✅`
    );
}

module.exports = { handleGST };
