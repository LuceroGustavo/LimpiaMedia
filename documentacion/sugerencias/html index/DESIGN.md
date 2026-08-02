---
name: LimpiaMedia
colors:
  surface: '#f7f9fb'
  surface-dim: '#d8dadc'
  surface-bright: '#f7f9fb'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f2f4f6'
  surface-container: '#eceef0'
  surface-container-high: '#e6e8ea'
  surface-container-highest: '#e0e3e5'
  on-surface: '#191c1e'
  on-surface-variant: '#464554'
  inverse-surface: '#2d3133'
  inverse-on-surface: '#eff1f3'
  outline: '#767586'
  outline-variant: '#c7c4d7'
  surface-tint: '#494bd6'
  primary: '#4648d4'
  on-primary: '#ffffff'
  primary-container: '#6063ee'
  on-primary-container: '#fffbff'
  inverse-primary: '#c0c1ff'
  secondary: '#505f76'
  on-secondary: '#ffffff'
  secondary-container: '#d0e1fb'
  on-secondary-container: '#54647a'
  tertiary: '#904900'
  on-tertiary: '#ffffff'
  tertiary-container: '#b55d00'
  on-tertiary-container: '#fffbff'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#e1e0ff'
  primary-fixed-dim: '#c0c1ff'
  on-primary-fixed: '#07006c'
  on-primary-fixed-variant: '#2f2ebe'
  secondary-fixed: '#d3e4fe'
  secondary-fixed-dim: '#b7c8e1'
  on-secondary-fixed: '#0b1c30'
  on-secondary-fixed-variant: '#38485d'
  tertiary-fixed: '#ffdcc5'
  tertiary-fixed-dim: '#ffb783'
  on-tertiary-fixed: '#301400'
  on-tertiary-fixed-variant: '#703700'
  background: '#f7f9fb'
  on-background: '#191c1e'
  surface-variant: '#e0e3e5'
typography:
  display:
    fontFamily: Inter
    fontSize: 36px
    fontWeight: '700'
    lineHeight: 44px
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Inter
    fontSize: 28px
    fontWeight: '600'
    lineHeight: 36px
    letterSpacing: -0.01em
  headline-md:
    fontFamily: Inter
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 28px
  body-lg:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  label-md:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '600'
    lineHeight: 16px
    letterSpacing: 0.01em
  headline-lg-mobile:
    fontFamily: Inter
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  base: 4px
  xs: 0.5rem
  sm: 1rem
  md: 1.5rem
  lg: 2rem
  xl: 3rem
  gutter: 1rem
  margin: 1.5rem
---

## Brand & Style
The design system is rooted in **Minimalism** with a **Glassmorphism Light** influence. It prioritizes clarity and utility, ensuring that the complex task of file management feels effortless and lightweight. The aesthetic focuses on "Soft UI" principles: high-quality whitespace, subtle depth through translucent layers, and a refined professional tone.

The target audience consists of users seeking a trustworthy tool to manage their local storage. The emotional response should be one of relief and confidence—moving from the "clutter" of duplicate files to a "clean" and organized digital space.

## Colors
The palette is dominated by a clean "Zinc and Slate" neutral base to maintain a utility-focused feel. 

- **Primary:** A soft indigo (#6366f1) used for primary actions and brand presence.
- **Surface Colors:** Use high-brightness neutrals (#f8fafc) for backgrounds to ensure the "glass" effects have enough contrast to be visible.
- **Category Colors:** Distinct hues are reserved strictly for file categorization (Photos, Videos, etc.) to allow for instant visual scanning.
- **Glass Effect:** Surfaces should use a semi-transparent white (rgba(255, 255, 255, 0.7)) with a 12px to 20px backdrop-blur.

## Typography
This design system utilizes **Inter** exclusively to lean into its systematic and utilitarian nature. 

- **Hierarchy:** Use font weight rather than size to differentiate information. Titles should be `SemiBold` (600) or `Bold` (700), while metadata and secondary info use `Regular` (400).
- **Scale:** On mobile devices, the display and large headlines scale down to prevent excessive word-wrapping, ensuring the UI remains compact and legible during file scans.
- **Functionality:** Use tabular figures for file sizes and counts to ensure numbers align perfectly in lists and tables.

## Layout & Spacing
The layout follows a **Fluid Grid** model with generous safe areas. 

- **Desktop:** 12-column grid with 24px gutters. Sidebars are fixed at 280px to maintain a steady navigation anchor.
- **Mobile:** 4-column grid with 16px margins.
- **Rhythm:** An 8px linear scale is used for all internal component spacing, while a 4px "half-step" is reserved for tight clusters like label-icon pairings.
- **Alignment:** Content is primarily left-aligned to mimic traditional file explorer patterns, providing a sense of familiarity.

## Elevation & Depth
Depth is created through **Tonal Layering** and **Subtle Shadows** rather than high-contrast borders.

- **Background:** The base layer is a solid neutral (#F8FAFC).
- **Surfaces:** Floating panels (like the duplicate list) use a "Glass" effect: 70% opacity white with a subtle 1px white border (inner glow) and a `backdrop-filter: blur(16px)`.
- **Shadows:** Use a single, highly-diffused shadow for elevated elements: `0 10px 25px -5px rgba(0, 0, 0, 0.04)`.
- **Interaction:** On hover, cards should slightly increase in shadow spread and scale (1.01x) to provide tactile feedback.

## Shapes
The shape language is friendly and modern, utilizing a **Rounded** (0.5rem) base.

- **Small Components:** Checkboxes and small tags use a 4px radius.
- **Medium Components:** Buttons, input fields, and standard cards use the `rounded-md` (8px) or `rounded-lg` (16px) setting.
- **Large Components:** Main content containers and "Complete" state modals use `rounded-xl` (24px).
- **Pills:** Status indicators (Completado, Error) always use a full pill radius (999px) to distinguish them from interactive buttons.

## Components
- **Buttons:** Primary buttons use the soft indigo background with white text. Secondary buttons use a light slate tint. All buttons feature a 0.2s transition on hover.
- **Status Pills:** 
    - *Completado*: Emerald tint background with dark emerald text.
    - *Error*: Rose tint background with dark rose text.
    - *En Progreso*: Indigo tint background with indigo text, featuring a subtle pulse animation.
- **File Cards:** Use a subtle 1px border (#e2e8f0). Include a large, category-colored icon (e.g., Blue for Photos) to the left of the filename.
- **Input Fields:** Soft grey backgrounds (#f1f5f9) that transition to a white background with a primary indigo border on focus.
- **Progress Bars:** Use a thick 8px track with rounded caps. The fill should use a gradient of the category color to indicate movement.
- **Empty States:** Use light-weighted line illustrations and "Body-LG" text to guide users to drag and drop folders.