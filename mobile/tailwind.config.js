/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ["./App.{js,jsx,ts,tsx}", "./src/**/*.{js,jsx,ts,tsx}"],
  presets: [require("nativewind/preset")],
  theme: {
    extend: {
      colors: {
        primary: "#25D366",
        "warn-muted": "#8B8F9A",
        loss: "#FF4444",
        dark: "#0B0D12",
        surface: "#16181F",
        "surface-elevated": "#1E212B",
        "text-primary": "#F2F2F0",
        "text-secondary": "#8B8F9A",
        border: "#262A35",
      },
      fontSize: {
        body: ["16px", { lineHeight: "24px" }],
        lg: ["18px", { lineHeight: "28px" }],
        xl: ["20px", { lineHeight: "28px" }],
        "2xl": ["24px", { lineHeight: "32px" }],
        "3xl": ["30px", { lineHeight: "36px" }],
        "4xl": ["36px", { lineHeight: "40px" }],
        money: ["28px", { lineHeight: "36px", fontWeight: "700" }],
      },
      spacing: {
        "thumb-zone": "60%",
      },
      borderRadius: {
        card: "16px",
        button: "12px",
      },
      minHeight: {
        cta: "56px",
        touch: "48px",
      },
      minWidth: {
        touch: "48px",
      },
    },
  },
  plugins: [],
};
