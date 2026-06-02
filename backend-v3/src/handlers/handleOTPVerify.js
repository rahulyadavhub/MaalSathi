// ============================================================
// src/handlers/handleOTPVerify.js
// OTP enter → verify → activate subscription
// ============================================================

'use strict';

const { verifyOTP }               = require('../services/otp/otpService');
const { activate }                = require('../services/subscription/subscriptionService');
const { sendMessage }             = require('../services/whatsapp/sender');
const { OTP_MESSAGES, OTP_CONFIG } = require('../constants/otpConfig');
const { createLogger }            = require('../utils/logger');
const log = createLogger('handleOTPVerify');

async function handleOTPVerify(user, otpInput) {
    try {
        const phone = user.phone;
        const otp   = (otpInput || '').trim();

        if (!otp || !/^\d{6}$/.test(otp)) {
            return `❌ OTP 6 digits ka hona chahiye\nExample: 847291`;
        }

        const result = await verifyOTP(phone, otp);

        if (result.locked) {
            return `🔒 Bahut zyada galat attempts\n${OTP_CONFIG.LOCK_MINUTES} minute baad try karo`;
        }

        if (result.expired) {
            return `⏰ OTP expire ho gaya\n"resend" likhke naya mangao`;
        }

        if (!result.valid) {
            const remaining = OTP_CONFIG.MAX_ATTEMPTS - result.attempts;
            return `❌ Galat OTP — ${remaining} chances bache hain`;
        }

        // OTP valid — activate subscription
        const payment = result.payment;
        if (!payment) {
            return `Kuch problem aayi — support se contact karo`;
        }

        await activate(user._id, payment.plan, payment._id);

        const planName = payment.plan.toUpperCase();

        log.info(`Subscription activated: ${phone} | plan: ${payment.plan}`);

        return (
            `🎉 ${planName} Plan Active Ho Gaya!\n\n` +
            `✅ Payment Verified\n` +
            `📅 Valid: 30 din\n\n` +
            `Ab ye features use karo:\n` +
            (payment.plan === 'pro'
                ? `• PDF reports\n• GST invoices\n• Document reminders\n• Daily summaries`
                : `• Multiple trucks\n• Driver management\n• Fleet analytics\n• Web dashboard`
            ) +
            `\n\nKoi sawaal ho toh "help" likho 🚛`
        );

    } catch (err) {
        log.error(`handleOTPVerify error: ${err.message}`);
        return `OTP verify nahi hua — thodi der baad try karo`;
    }
}

module.exports = { handleOTPVerify };
