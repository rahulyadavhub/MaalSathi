// ============================================================
// src/handlers/handleSubscription.js
// "Pro lena hai" → payment link flow
// ============================================================

'use strict';

const { createOrder }           = require('../services/payment/razorpayService');
const { hasAccess, upgradeMessage } = require('../middleware/checkSubscription');
const { PLAN_CONFIG }           = require('../constants/subscriptionPlans');
const { sendMessage }           = require('../services/whatsapp/sender');
const { createLogger }          = require('../utils/logger');
const log = createLogger('handleSubscription');

async function handleSubscription(user, message) {
    try {
        const text = (message || '').toLowerCase().trim();
        const currentPlan = user.subscription?.plan || 'free';

        // Show plan info
        if (text === 'plan' || text === 'mera plan' || text === 'subscription') {
            return await sendPlanInfo(user, currentPlan);
        }

        // Upgrade to PRO
        if (text.includes('pro') || text.includes('upgrade') || text.includes('199') || text.includes('299')) {
            if (currentPlan === 'pro' || currentPlan === 'fleet') {
                return `✅ Tum already ${currentPlan.toUpperCase()} plan pe ho!\n\nPlan khatam hone se pehle renew karo.`;
            }
            return await initiatePayment(user, 'pro');
        }

        // Upgrade to FLEET
        if (text.includes('fleet') || text.includes('999')) {
            if (currentPlan === 'fleet') {
                return `✅ Tum already FLEET plan pe ho!`;
            }
            return await initiatePayment(user, 'fleet');
        }

        return await sendPlanInfo(user, currentPlan);

    } catch (err) {
        log.error(`handleSubscription error: ${err.message}`);
        return `Kuch problem aayi — thodi der baad try karo`;
    }
}

async function sendPlanInfo(user, currentPlan) {
    const plan = PLAN_CONFIG[currentPlan];

    return (
        `📋 Tera Current Plan: ${plan.name}\n\n` +
        `━━━━━━━━━━━━━━━━━━\n` +
        `🆓 FREE — ₹0\n` +
        `  • 10 trips/month\n` +
        `  • Basic P&L\n\n` +
        `⭐ PRO — ₹299/month\n` +
        `  • Unlimited trips\n` +
        `  • PDF reports\n` +
        `  • GST invoices\n` +
        `  • Document reminders\n` +
        `  • Daily + weekly summary\n\n` +
        `🚛 FLEET — ₹999/month\n` +
        `  • Multiple trucks\n` +
        `  • Driver management\n` +
        `  • Fleet analytics\n` +
        `  • Web dashboard\n\n` +
        `Upgrade ke liye likho:\n"pro lena hai" ya "fleet lena hai"`
    );
}

async function initiatePayment(user, plan) {
    try {
        const config = PLAN_CONFIG[plan];
        const order  = await createOrder(
            user._id,
            user.phone,
            plan,
            config.price
        );

        return (
            `🚀 ${config.label}\n\n` +
            `💰 Amount: ₹${config.price / 100}/month\n\n` +
            `👉 Pay karo:\n` +
            `https://razorpay.com/pay/${order.id}\n\n` +
            `Payment ke baad OTP SMS aayega\n` +
            `OTP enter karo → Plan activate! ✅`
        );

    } catch (err) {
        log.error(`initiatePayment error: ${err.message}`);
        return `Payment link generate nahi hua — thodi der baad try karo`;
    }
}

module.exports = { handleSubscription };
