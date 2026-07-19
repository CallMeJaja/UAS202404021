# Design System: Stokify

## 1. Visual Theme & Atmosphere

A clean, functional, professional inventory management interface. The density is balanced at 5/10 — not too airy, not too dense — optimized for daily operational use by warehouse staff and business owners. The atmosphere is clinical yet approachable, like a well-organized workspace. Asymmetric layouts for dashboard sections, symmetric for form-based screens. Confident use of whitespace to separate functional zones. Subtle spring-physics micro-interactions on interactive elements provide tactile feedback without distraction.

## 2. Color Palette & Roles

- **Forest Canopy** (#4CAF50) — Primary accent for CTAs, active navigation states, focus rings, and positive stock indicators. Used strategically for actions that move the business forward.
- **Midnight Ink** (#1C1B1F) — Primary text color, Zinc-950 depth. Maximum legibility for data and labels.
- **Muted Steel** (#757575) — Secondary text, descriptions, metadata, and placeholder content.
- **Light Gray** (#9E9E9E) — Tertiary text, disabled states, and subtle hints.
- **Whisper Border** (#E0E0E0) — Card borders, input outlines, and structural dividers. 1px structural lines.
- **Canvas White** (#FAFAFA) — Primary background surface. Clean, uncluttered base.
- **Pure Surface** (#FFFFFF) — Card and container fill. Elevated elements sit on white.
- **Safety Green** (#4CAF50) — In-stock status indicator. Background tint: light green.
- **Warning Amber** (#FF9800) — Low-stock status indicator. Background tint: light orange.
- **Danger Crimson** (#F44336) — Out-of-stock, error states, destructive actions. Background tint: light red.
- **Admin Badge** (#4CAF50) — Green pill badge for Admin role identification.
- **Staff Badge** (#2196F3) — Blue pill badge for Staff role identification.

## 3. Typography Rules

- **Display/Headlines:** Plus Jakarta Sans — Track-tight, controlled scale, weight-driven hierarchy. Bold (700) for primary headings, Semibold (600) for section headers.
- **Body:** Plus Jakarta Sans — Regular (400), relaxed leading (1.5), maximum 65 characters per line for readability.
- **Mono:** JetBrains Mono — For SKU codes, quantities, timestamps, and numerical data alignment in columns.
- **Banned:** Inter is banned for this project. Generic serif fonts (Times New Roman, Georgia, Garamond) are banned. All typography uses the Plus Jakarta Sans family for cohesion.
- **Scale:** Display 32px/700, Headline 24px/600, Title 20px/600, Body-Large 16px/400, Body-Medium 14px/400, Label 12px/500.

## 4. Component Stylings

- **Buttons:** Primary buttons are pill-shaped with Forest Canopy fill and white text. Secondary buttons use ghost/outline style with Muted Steel text. Tactile -1px translate on active state. No neon outer glows. Minimum 44px touch target.
- **Cards/Containers:** Softly rounded corners (8px radius). Whisper-soft diffused shadow for elevation. 1px Whisper Border for structural definition. Used only when elevation communicates hierarchy.
- **Inputs/Forms:** Label above input field, helper text optional, error text below in Danger Crimson. Outlined style with 1px Whisper Border. Focus ring in Forest Canopy. No floating labels.
- **Chips/Badges:** Low-saturation background tints with high-saturation text. Pill-shaped for status indicators. Category chips use neutral tints. Status chips use semantic colors (green/orange/red).
- **Bottom Navigation:** 3-tab bar (Dashboard, Katalog, Profil). Active tab uses Forest Canopy color with filled icon. Inactive tabs use Muted Steel with outlined icons.
- **Snackbar/Toast:** Forest Canopy background with white text and checkmark icon. Positioned at bottom with rounded corners and subtle shadow. Auto-dismiss after 3 seconds.
- **Dialog/Modal:** Centered white card with generous padding. Semi-transparent dark overlay (40% black) behind. Rounded corners (16px). Title, body, and action buttons with clear hierarchy.
- **Empty States:** Composed illustrated compositions with centered icon, descriptive headline, helper text, and primary CTA. Not just "No data" text.
- **Loading States:** Skeletal shimmer matching exact layout dimensions. No circular spinners.
- **FAB (Floating Action Button):** Forest Canopy circle with white plus icon. Positioned bottom-right with whisper shadow.

## 5. Layout Principles

- Mobile-first single column layout for all screens.
- 8px spacing grid for consistent rhythm.
- Vertical section gaps use 32px or 48px increments.
- Internal component spacing uses 8px or 16px.
- Max-width containment for dashboard content areas.
- No horizontal scroll on mobile — critical failure.
- All interactive elements minimum 44px touch target.
- Bottom navigation fixed at screen bottom.
- Top app bar fixed at screen top with back button and title.
- Forms use full-width inputs with 16px horizontal padding.

## 6. Motion & Interaction

- Spring physics default: stiffness 100, damping 20 — premium, weighty feel.
- Staggered cascade reveals for list items — never mount lists instantly.
- Hardware-accelerated transforms only (translate, opacity, scale).
- Never animate top, left, width, height directly.
- Micro-interactions on buttons: -1px translate on active, smooth return.
- Snackbar slide-up entry animation.
- Dialog fade-in with scale animation.
- Skeleton shimmer with infinite loop for loading states.

## 7. Anti-Patterns (Banned)

- No emojis anywhere in the interface
- No Inter font — Plus Jakarta Sans only
- No pure black (#000000) — use Midnight Ink (#1C1B1F)
- No neon or outer glow shadows
- No oversaturated accents — keep colors calibrated
- No excessive gradient text on large headers
- No 3-column equal card layouts — use asymmetric grids
- No generic placeholder names — use contextual Indonesian names
- No fabricated data — use realistic inventory examples
- No AI copywriting clichés ("Elevate", "Seamless", "Next-Gen")
- No filler UI text ("Scroll to explore", "Swipe down")
- No broken image links — use solid color placeholders
- No overlapping elements — clean spatial separation always
