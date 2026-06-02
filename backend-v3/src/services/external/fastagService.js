// ============================================================
// src/services/external/fastagService.js
// FASTag balance check + recharge info
// ============================================================

'use strict';

const { createLogger } = require('../../utils/logger');
const log = createLogger('fastagService');

// FASTag issuers in India
const FASTAG_ISSUERS = {
    paytm:    { name: 'Paytm Payments Bank', helpline: '1800-120-4210', rechargeUrl: 'paytm.com/fastag' },
    hdfc:     { name: 'HDFC Bank',           helpline: '1800-120-1243', rechargeUrl: 'hdfcbank.com/fastag' },
    icici:    { name: 'ICICI Bank',          helpline: '1800-200-3344', rechargeUrl: 'icicibank.com/fastag' },
    sbi:      { name: 'SBI',                 helpline: '1800-11-2211',  rechargeUrl: 'sbi.co.in/fastag' },
    axis:     { name: 'Axis Bank',           helpline: '1800-419-6969', rechargeUrl: 'axisbank.com/fastag' },
    kotak:    { name: 'Kotak Bank',          helpline: '1860-266-0811', rechargeUrl: 'kotak.com/fastag' },
    airtel:   { name: 'Airtel Payments Bank',helpline: '1800-103-6065', rechargeUrl: 'airtel.in/bank/fastag' },
    iob:      { name: 'Indian Overseas Bank',helpline: '1800-425-4445', rechargeUrl: 'iob.in/fastag' },
};

// NHAI helpline
const NHAI_HELPLINE = '1033';

// Get FASTag recharge info
function getRechargeInfo(issuer = null) {
    if (issuer) {
        const key = issuer.toLowerCase();
        const info = FASTAG_ISSUERS[key];
        if (info) {
            return {
                issuer: info.name,
                helpline: info.helpline,
                rechargeUrl: info.rechargeUrl,
                found: true,
            };
        }
    }

    // Return all options if no issuer specified
    return {
        found: false,
        issuers: Object.values(FASTAG_ISSUERS),
        nhai_helpline: NHAI_HELPLINE,
    };
}

// Low balance alert threshold
const LOW_BALANCE_THRESHOLD = 200;  // ₹200

function isLowBalance(balance) {
    return balance < LOW_BALANCE_THRESHOLD;
}

// Format FASTag message for WhatsApp
function formatFastagMessage(issuer = null) {
    if (issuer) {
        const info = getRechargeInfo(issuer);
        if (info.found) {
            return (
                `💳 FASTag Recharge — ${info.issuer}\n\n` +
                `🌐 Website: ${info.rechargeUrl}\n` +
                `📞 Helpline: ${info.helpline}\n\n` +
                `Ya PhonePe/GPay/Paytm se bhi recharge kar sakte ho\n` +
                `⚠️ Balance ₹${LOW_BALANCE_THRESHOLD} se kam = double toll!`
            );
        }
    }

    return (
        `💳 FASTag Recharge Kaise Karein?\n\n` +
        `1️⃣ PhonePe / GPay / Paytm\n` +
        `   FASTag section → Vehicle number → Recharge\n\n` +
        `2️⃣ Bank App\n` +
        `   HDFC / ICICI / SBI / Axis / Kotak\n\n` +
        `3️⃣ NHAI Helpline: ${NHAI_HELPLINE}\n\n` +
        `⚠️ Balance ₹${LOW_BALANCE_THRESHOLD} se kam ho toh turant recharge karo!\n` +
        `Double toll lagta hai low balance pe.`
    );
}

// Format low balance alert
function formatLowBalanceAlert(vehicleNumber, balance) {
    return (
        `⚠️ FASTag Low Balance Alert!\n\n` +
        `🚛 Vehicle: ${vehicleNumber}\n` +
        `💰 Balance: ₹${balance}\n\n` +
        `Double toll se bachne ke liye abhi recharge karo!\n` +
        `PhonePe/GPay/Paytm se 2 minute mein ho jaayega 📱`
    );
}

module.exports = {
    getRechargeInfo,
    isLowBalance,
    formatFastagMessage,
    formatLowBalanceAlert,
    FASTAG_ISSUERS,
    LOW_BALANCE_THRESHOLD,
};
