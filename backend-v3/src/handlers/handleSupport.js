// ============================================================
// src/handlers/handleSupport.js
// "Problem hai" → 3-level customer care flow
// ============================================================

'use strict';

const { createTicket }       = require('../services/support/supportService');
const { notifyAdmin }        = require('../services/support/ticketNotifier');
const { getByNumber, resolve } = require('../services/support/faqResolver');
const { SUPPORT_MESSAGES }   = require('../constants/supportConfig');
const { createLogger }       = require('../utils/logger');
const log = createLogger('handleSupport');

async function handleSupport(user, message, convState) {
    try {
        const text = (message || '').toLowerCase().trim();
        const plan = user.subscription?.plan || 'free';

        // User selected FAQ option (1-4)
        if (/^[1-4]$/.test(text) && convState === 'support_menu') {
            const answer = getByNumber(text);
            if (answer) return answer;
        }

        // User typed "5" or unknown — escalate
        if (text === '5' || convState === 'support_menu') {
            return await escalateToHuman(user, message, plan);
        }

        // Try auto-resolve from FAQ
        const autoAnswer = resolve(message);
        if (autoAnswer) {
            log.info(`FAQ auto-resolved for ${user.phone}`);
            return autoAnswer;
        }

        // Show support menu
        return SUPPORT_MESSAGES.MENU;

    } catch (err) {
        log.error(`handleSupport error: ${err.message}`);
        return `Kuch problem aayi — thodi der baad try karo`;
    }
}

async function escalateToHuman(user, issue, plan) {
    try {
        // Free users — bot only
        if (plan === 'free') {
            return (
                `🆓 Free plan mein sirf bot support available hai\n\n` +
                `Upgrade karo PRO mein:\n` +
                `✅ WhatsApp support (24hr)\n` +
                `✅ Priority response\n\n` +
                `"upgrade" likhke ₹299/month mein switch karo`
            );
        }

        // PRO + FLEET — create ticket
        const ticket = await createTicket({
            userId:   user._id,
            phone:    user.phone,
            userName: user.name,
            issue,
            plan,
        });

        // Notify Nick
        await notifyAdmin(ticket);

        const sla = plan === 'fleet' ? '4 ghante' : '24 ghante';

        return (
            `✅ Ticket ${ticket.ticketId} create ho gaya!\n\n` +
            `Team ${sla} mein contact karegi\n` +
            `Phone: +${user.phone}\n\n` +
            (plan === 'fleet'
                ? `📞 Call book karo: calendly.com/maalsaathi/support`
                : `WhatsApp pe reply ka intezaar karo 🙏`
            )
        );

    } catch (err) {
        log.error(`escalateToHuman error: ${err.message}`);
        return `Ticket create nahi hua — directly +91XXXXXXXXXX pe WhatsApp karo`;
    }
}

module.exports = { handleSupport };
