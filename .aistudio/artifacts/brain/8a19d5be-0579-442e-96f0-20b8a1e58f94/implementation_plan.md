# Quantia — Material 3 Expressive Scientific Calculator & Mathematical Studio

Quantia is a flagship Android scientific calculator and mathematical studio that pairs **Material 3 Expressive** visual language—morphing button shapes, scalloped cookie badges, wavy progress tracks, and high-contrast editorial typography—with **115+ real mathematical, graphing, linear algebra, statistical, financial, and engineering features**. It delivers instant, offline-first precision with zero-latency live evaluation and a tactile, customizable workspace inspired by the provided Material You reference designs.

### User Review & Critical Decisions

> [!IMPORTANT]
> All core product, aesthetic, interaction, and storage decisions have been aligned based on your selections:

- **Confirmed Decision 1 — Visual Theme & Color Palette**: **Dynamic Material You with 4 Expressive Presets** (*Sage Leafit*, *Violet Playground*, *Warm Terracotta*, and *Deep Mint*, plus system wallpaper Dynamic Color on Android 12+ and independent Light/Dark mode toggles).
- **Confirmed Decision 2 — 100+ Feature Scope across 5 Mathematical Suites**: Full inclusion of all 5 major mathematical suites (**118 distinct features** total):
  1. *Advanced Scientific, Complex Numbers, and Calculus Engine*
  2. *Interactive 2D Function Graphing and Table Generator*
  3. *Matrix, Vector, Linear Algebra, and Equation Solvers*
  4. *30+ Unit Converters, 50+ Physics Constants, and Base-N Programmer Mode*
  5. *Statistics, Probability Distributions, and Financial Calculators*
- **Confirmed Decision 3 — Keypad & Expressive Interaction Style**: **Morphing Shapes, Expandable Function Sheet, and Floating Operator Hub**, featuring squircle-to-circle press morphing, a signature 2×2/docked operator cluster with a scalloped `=` / action badge, and an expandable scientific drawer.
- **Confirmed Decision 4 — Data Persistence**: **Local on-device database (Room + DataStore)** for instant offline speed, privacy, persistent calculation history tape, pinned expressions, custom user variables (`A`–`Z`), and saved custom formulas.

---

### 1. Overview & Core Concept

- **What It Does**: Quantia unifies a high-precision symbolic and numeric scientific calculator with interactive multi-function 2D graphing, symbolic/numeric calculus, matrix & vector workspaces, polynomial/system equation solvers, statistical & probability distribution analyzers, financial TVM/loan tools, Base-N programmer bitwise operations, 50+ NIST physics constants, and 30+ unit conversion categories.
- **Target Audience / Persona**: Engineers, STEM students, researchers, programmers, financial analysts, and design-conscious power users who want desktop-grade mathematical depth wrapped in a joyful, tactile Material 3 Expressive interface.
- **Key Value**: Eliminates the trade-off between utilitarian graphing calculators and modern mobile UX by making 118+ tools immediately discoverable through expressive pill toolbars, live-preview chips, and contextual bottom sheets.

#### Complete 118-Feature Catalog Across 5 Suites

1. **Suite 1: Core & Advanced Scientific, Complex Numbers & Calculus (36 Features)**
   - *Standard & Algebraic (10)*: Addition, subtraction, multiplication, division, modulo (`mod`), percentage (`%`), power (`x^y`), square (`x²`), cube (`x³`), reciprocal (`1/x`).
   - *Roots & Logarithms (6)*: Square root (`√x`), cube root (`∛x`), nth root (`ⁿ√x`), natural log (`ln`), base-10 log (`log₁₀`), arbitrary base log (`log_b(x)`), exponential (`e^x`, `10^x`, `2^x`).
   - *Trigonometry & Hyperbolics in DEG / RAD / GRAD (12)*: `sin`, `cos`, `tan`, `cot`, `sec`, `csc`, inverse (`asin`, `acos`, `atan`), and hyperbolic (`sinh`, `cosh`, `tanh`, `asinh`, `acosh`, `atanh`).
   - *Number Theory & Combinatorics (5)*: Factorial (`n!`), Gamma function (`Γ(x)`), Permutation (`nPr`), Combination (`nCr`), Greatest Common Divisor (`gcd`), Least Common Multiple (`lcm`), Prime Factorization (`factor`), Floor/Ceiling/Round/Abs/Sign.
   - *Complex Numbers & Calculus (3+)*: Full rectangular (`a + bi`) & polar (`r∠θ`) arithmetic, magnitude (`|z|`), argument (`arg(z)`), complex conjugate (`conj`), numerical definite derivative (`d/dx` at `x = a`), definite integral (`∫ f(x) dx` via Simpson's rule), and summation/product series (`Σ`, `Π`).

2. **Suite 2: Interactive 2D Function Graphing & Table Generator (14 Features)**
   - Multi-function plotting (up to 4 simultaneous color-coded functions `f₁(x)`–`f₄(x)`), pinch-to-zoom & pan canvas, interactive crosshair trace with live `(x, y)` coordinates, automatic root/zero finder, local extrema (min/max) detector, curve intersection finder, tangent line & slope visualization at any `x`, shaded definite integral area under curve, polar/parametric preview toggle, dynamic `x / f(x)` table generator with custom step `Δx`, graph bookmarking to local database, and one-tap coordinate copy.

3. **Suite 3: Matrix, Vector, Linear Algebra & Equation Solvers (18 Features)**
   - *Matrix Workspace (up to 4×4)*: Matrix addition (`A + B`), subtraction (`A - B`), scalar & matrix multiplication (`A × B`), determinant (`det(A)`), transpose (`Aᵀ`), matrix inverse (`A⁻¹`), trace (`tr(A)`), rank, Row Reduced Echelon Form (`RREF`), eigenvalue estimation (2×2/3×3), and power (`Aⁿ`).
   - *Vector Operations (2D & 3D)*: Dot product (`u · v`), cross product (`u × v`), vector magnitude (`‖u‖`), unit vector normalization, angle between vectors, scalar projection.
   - *Equation Solvers*: Quadratic & cubic exact polynomial root solver (including complex roots) and 2×2 / 3×3 linear system solver (`Ax = b` with Cramer's / Gaussian elimination steps).

4. **Suite 4: 30+ Unit Converters, 50+ Physics Constants & Base-N Programmer Mode (32 Features)**
   - *30 Unit Converter Categories*: Length, Mass, Temperature, Area, Volume, Speed, Time, Digital Storage, Data Transfer Rate, Energy, Power, Pressure, Force, Angle, Frequency, Fuel Economy, Torque, Density, Dynamic Viscosity, Kinematic Viscosity, Electric Current, Voltage, Resistance, Capacitance, Inductance, Magnetic Flux, Illuminance, Radiation Dose, Typography/CSS Units, and Cooking Volume.
   - *50+ CODATA Physics, Chemistry & Math Constants*: Speed of light (`c`), Planck (`h`, `ℏ`), Gravitational (`G`), Boltzmann (`k_B`), Avogadro (`N_A`), Elementary charge (`e`), Electron/Proton/Neutron masses, Gas constant (`R`), Stefan-Boltzmann (`σ`), Vacuum permittivity (`ε₀`), Permeability (`μ₀`), Fine-structure (`α`), Rydberg (`R_∞`), Faraday (`F`), Bohr radius (`a₀`), Bohr magneton (`μ_B`), Standard gravity (`g`), Standard atmosphere (`atm`), Hubble, Solar mass/radius, Earth mass/radius, Golden ratio (`φ`), Euler-Mascheroni (`γ`), Catalan's constant, Apéry's constant, etc.—searchable and one-tap insertable.
   - *Base-N Programmer Mode*: Live simultaneous `HEX`, `DEC`, `OCT`, `BIN` conversion, selectable word sizes (`64-bit`, `32-bit`, `16-bit`, `8-bit`, signed/unsigned 2's complement), interactive bit-toggle grid, bitwise `AND`, `OR`, `XOR`, `NOT`, `NAND`, `NOR`, Left Shift (`<<`), Right Shift (`>>`), and Rotate (`ROL`/`ROR`).

5. **Suite 5: Statistics, Probability Distributions & Financial Calculators (18 Features)**
   - *Descriptive Statistics & Regression*: Interactive dataset editor with Mean (`x̄`), Median, Mode, Sample & Population Standard Deviation (`s`, `σ`), Variance (`s²`, `σ²`), Quartiles (`Q1`, `Q2`, `Q3`, `IQR`), Min/Max/Range, Z-score calculator, and 2-variable Linear Regression (`y = ax + b` with correlation `r` and `R²`) with an expressive bar/scatter mini-chart.
   - *Probability Distributions*: Normal (`PDF`, `CDF`, two-tailed area), Binomial (`P(X = k)`, `P(X ≤ k)`), and Poisson distribution calculators with visual wavy progress bars.
   - *Financial Suite*: Time Value of Money / Compound Interest solver, Loan & Mortgage Amortization calculator (monthly payment, total interest, principal vs. interest breakdown), Return on Investment (`ROI` & `CAGR`), Margin/Markup/Discount & Tax calculator, and Tip/Split bill calculator.

---

### 2. User Experience & Visual Design

- **Key User Flows**:
  1. **Expressive Scientific Workspace (Home)**: Opens directly to a dramatic, asymmetric display card showing the live-formatted mathematical expression, an instant result pill badge (styled after the `$1,306.29` pill in your reference image), quick status chips (`DEG/RAD/GRAD`, `DEC/FRAC/SCI`, active memory pill), and the morphing keypad below.
  2. **Floating Operator Hub & Scalloped Action Badge**: Inspired directly by your budget calculator screenshot, core operators (`+`, `−`, `×`, `÷`, `=`) are visually anchored with high-contrast expressive shapes, including a signature **12-point scalloped cookie shape** for the primary evaluate (`=`) / action badge and a tag-styled backspace key.
  3. **Expandable Function Sheet & Quick-Insert Drawer**: Pulling up or tapping the expressive pill handle reveals categorized scientific chips (`Trig`, `Calculus`, `Complex`, `Combinatorics`, `Constants`, `Variables A–Z`), letting users insert advanced functions or physical constants without cluttering the primary keys.
  4. **Segmented Suite Switcher**: A top/bottom Material 3 Expressive segmented pill bar switches smoothly between **Scientific**, **Graphing**, **Algebra & Matrices**, **Convert & Base-N**, and **Stats & Finance**, plus a slide-over **History & Variables Tape** and **Theme Studio**.

- **Visual Identity & Theme**:
  - *Aesthetic Direction*: **Material 3 Expressive (Android 16 / Material You Evolution)**—combining organic geometric shapes (scalloped cookies, squircles, pills, circles), bold display numerals, wavy sine-wave progress/slider tracks, and warm, tactile surface hierarchy.
  - *Color Palette & Mood*: Supports system **Dynamic Color** on Android 12+ alongside **4 curated Expressive Presets** (each with tailored Light and Dark schemes):
    - **Sage Leafit (Default)**: Warm Alabaster background (`#F6F8F3` / dark `#111714`), Soft Sage containers (`#DCE6DF`), Deep Forest primary (`#245643`), Warm Amber & Lavender accents (`#F3D59B`, `#E5DDF5`).
    - **Violet Playground**: Soft Lilac canvas (`#FAF5FF` / dark `#16111E`), Electric Violet cards (`#B689FF`), Deep Plum ink (`#2A1447`), Coral-Peach accent (`#FF8C7A`).
    - **Warm Terracotta**: Cream Sand canvas (`#FDF8F2` / dark `#1C1512`), Espresso cards (`#3B2316`), Terracotta Coral primary (`#E06D53`), Pistachio Green scalloped badge (`#95C949`).
    - **Deep Mint & Teal**: Cool Mint canvas (`#EFFAF8` / dark `#0D1B1E`), Cyan-Mint primary (`#5CE1E6`), Deep Slate containers (`#163038`), Warm Pink-Coral accent (`#FF6B8B`).
  - *Typography & Hierarchy*:
    - **Display & Tall Numerals (`Space Grotesk`)**: Used for the giant live expression, result readout, and widget-style metric headers with tight tracking and tabular numeral alignment.
    - **Editorial Accent (`Instrument Serif` / `Playfair Display`)**: Used for mathematical function headers, constant symbols, and empty-state callouts (echoing the serif notes in your reference images).
    - **Precision Code & Base-N (`JetBrains Mono`)**: Used for binary/hex bit grids, matrix entries, and step-by-step equation readouts.
  - *Component Styling & Layout*:
    - **Custom Canvas Shapes**: Reusable `ScallopedCookieShape` (8- and 12-lobe sinusoidal polygon), `WavyProgressTrack` (sine-wave Canvas indicator inspired by the Leafit calorie bar), and animated corner-radius morphing on keypad press.
    - **Edge-to-Edge & Adaptive**: Full `enableEdgeToEdge()` safe-area inset handling and responsive layout that switches to a two-pane side-by-side view (Keypad + Live Graph/History/Suites) on expanded/landscape screens.

- **Interactive Feedback & Motion**:
  - **Shape Morphing Keys**: Pressing a circular or rounded keypad button smoothly animates its corner radius (`animateDpAsState` with bouncy `spring(dampingRatio = 0.6f)`) from a circle/pill into a squircle while scaling subtly and triggering Android haptic feedback (`LocalHapticFeedback`).
  - **Live Result Pill**: Evaluates expressions on every keystroke; tapping the result pill cycles between **Standard Decimal**, **Exact Fraction (`p/q`)**, **Engineering/Scientific (`a × 10ⁿ`)**, and **DMS (`° ′ ″`)**.

---

### 3. Key Product Decisions & Trade-Offs

- **Decision 1: Self-Contained Recursive-Descent & Symbolic Math Engine**
  - *Chosen Approach*: Build a dedicated, zero-dependency Kotlin mathematical parser and evaluator supporting real and complex numbers, implicit multiplication (`2π`, `3sin(45)`), nested functions, symbolic differentiation/numerical integration, exact rational fraction approximation (continued fractions), and arbitrary variable bindings (`A`–`Z`, `Ans`).
  - *Why*: External Java math libraries often lack complex-number overload, exact fraction conversion, or custom angle-unit awareness (`DEG/RAD/GRAD`). A purpose-built engine guarantees 100% deterministic behavior across all 118 features and instant live preview as the user types.
- **Decision 2: Unified Multi-Suite Architecture with Shared Memory & History**
  - *Chosen Approach*: Any result computed in the Matrix, Statistics, Financial, or Converter suites can be pinned or sent directly to the main calculator's `Ans` or `A`–`Z` variable slots with one tap.
  - *Why*: Prevents the 5 suites from feeling like disconnected mini-apps; every tool feeds into a cohesive mathematical workflow.
- **Decision 3: Local Room Database + DataStore Preferences**
  - *Chosen Approach*: Use **Room** (with KSP and reactive `Flow<List<...>>`) for calculation history, starred/pinned expressions, user variables, and custom saved formulas; use **DataStore Preferences** for the active Expressive Theme Preset, angle unit (`DEG/RAD/GRAD`), number format, and haptic toggle.
  - *Why*: Honors your choice for instant offline speed and complete privacy while ensuring zero data loss across app restarts.

---

### 4. Technical Architecture & Data Strategy *(Technical Reference)*

#### Architecture & Component Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                       UI Layer (Jetpack Compose + M3)                       │
│                                                                             │
│  ┌──────────────────────┐  ┌─────────────────────────────────────────────┐  │
│  │ Expressive Top Bar   │  │ Segmented Pill Navigation & Suite Switcher  │  │
│  │ (Theme, History,     │  │ [Scientific] [Graph] [Matrix] [Conv] [Stat] │  │
│  │  Constants, Memory)  │  └─────────────────────────────────────────────┘  │
│  └──────────┬───────────┘                                                   │
│             ▼                                                               │
│  ┌───────────────────────────────────────────────────────────────────────┐  │
│  │                    Active Workspace / Suite Screen                    │  │
│  │                                                                       │  │
│  │  ┌─────────────────────────┐  ┌────────────────────────────────────┐  │  │
│  │  │ Scientific & Complex    │  │ 2D Graphing & Table Studio         │  │  │
│  │  │ • Asymmetric Display    │  │ • Multi-Curve Canvas (Zoom/Pan)    │  │  │
│  │  │ • Live Result Pill      │  │ • Root / Extrema / Integral Shade  │  │  │
│  │  │ • Morphing Keypad       │  │ • Dynamic (x, f(x)) Table          │  │  │
│  │  │ • Scalloped '=' Hub     │  └────────────────────────────────────┘  │  │
│  │  │ • Expandable Fn Sheet   │  ┌────────────────────────────────────┐  │  │
│  │  └─────────────────────────┘  │ Matrix, Vector & Equation Solvers  │  │  │
│  │  ┌─────────────────────────┐  │ • 4x4 Grid, Det, Inv, RREF, Eigen  │  │  │
│  │  │ Convert, Constants &    │  │ • 2D/3D Vectors, Poly & Linear Sys │  │  │
│  │  │ Base-N Programmer       │  └────────────────────────────────────┘  │  │
│  │  │ • 30 Unit Categories    │  ┌────────────────────────────────────┐  │  │
│  │  │ • 50+ NIST Constants    │  │ Statistics, Distributions & Finance│  │  │
│  │  │ • HEX/DEC/OCT/BIN Grid  │  │ • Wavy Progress Cards, Regression  │  │  │
│  │  └─────────────────────────┘  │ • Normal/Binomial/Poisson, TVM/Loan│  │  │
│  └───────────────────────────────────────────────────────────────────────┘  │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │ StateFlow / UI Events
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                     ViewModel & Domain Calculation Layer                    │
│                                                                             │
│  ┌──────────────────────────────┐  ┌─────────────────────────────────────┐  │
│  │ CalculatorViewModel          │  │ Specialized Math Engines            │  │
│  │ • Expression & Cursor State  │  │ • ExpressionParser & AST Evaluator  │  │
│  │ • Live Evaluation & Formats  │  │ • ComplexNumber & FractionRational  │  │
│  │ • Graph Viewport & Traces    │  │ • CalculusEngine (d/dx, ∫, Σ, Π)    │  │
│  │ • Matrix/Stat/Finance State  │  │ • MatrixLinearAlgebra & PolySolver  │  │
│  │ • Theme & Preference State   │  │ • UnitConverter & ConstantsCatalog  │  │
│  └──────────────┬───────────────┘  └─────────────────────────────────────┘  │
└─────────────────┼───────────────────────────────────────────────────────────┘
                  │ Repository Pattern (Flow + suspend)
                  ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                      Local Persistence Layer (On-Device)                    │
│                                                                             │
│  ┌──────────────────────────────────────┐  ┌─────────────────────────────┐  │
│  │ Room Database (QuantiaDatabase)      │  │ DataStore Preferences       │  │
│  │ • CalculationHistoryEntity (Tape)    │  │ • ExpressiveThemePreset     │  │
│  │ • SavedVariableEntity (A–Z, Memory)  │  │ • DarkMode / DynamicColor   │  │
│  │ • CustomFormulaEntity (User Fns)     │  │ • AngleUnit & OutputFormat  │  │
│  └──────────────────────────────────────┘  └─────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────────┘
```

#### Data Model & State
- **`CalculationHistoryEntity` (Room)**: Stores `id`, `expression`, `result`, `formattedFraction`, `suiteType`, `isPinned`, and `timestamp`. Exposed as `Flow<List<CalculationHistoryEntity>>` for the interactive History Tape with search, pin, copy, and recall.
- **`SavedVariableEntity` (Room)**: Stores `symbol` (`A`–`Z`, `M`), `numericValue`, `complexImaginary`, `label`, and `updatedAt` so variables persist across sessions and can be referenced directly inside expressions (e.g., `2*A + sin(B)`).
- **`CustomFormulaEntity` (Room)**: Stores user-created or pre-seeded engineering/finance formulas (`id`, `title`, `category`, `expressionTemplate`, `variablesList`, `isFavorite`) that open in an interactive variable-prompt sheet.
- **`CalculatorUiState` (StateFlow)**: Holds current token buffer, cursor position, live evaluation preview, error diagnostic (if incomplete/invalid), angle mode (`DEG`, `RAD`, `GRAD`), result format (`DEC`, `FRAC`, `SCI`, `DMS`), `2nd`/`HYP` modifier states, active suite tab, and active theme preset.

#### Interactive Component & State Mapping
- **Morphing Keypad & Floating Operator Hub**: Dispatches token insertions, cursor movements, smart parentheses balancing, and backspace/clear actions to `CalculatorViewModel`. Pressing `=` commits the calculation to Room history and triggers a spring bounce on the result pill.
- **Interactive 2D Graph Canvas**: Uses `pointerInput` (`detectTransformGestures` and `detectTapGestures`) to update viewport bounds `(xMin, xMax, yMin, yMax)` and trace coordinate `xTrace`, recomputing roots, local extrema, tangent line, and shaded definite integral area in real time.
- **Matrix & Linear Algebra Grid**: Interactive cell editor supporting dimensions from `2×2` to `4×4` for Matrices `A` and `B`, with instant one-tap operations (`det`, `inv`, `RREF`, `A×B`, `Eigenvalues`) and a "Swap A ↔ B" or "Copy Result to A" workflow.
- **Converter, Constants & Base-N Suite**: Searchable chip filter across 30 unit categories with bidirectional live conversion, searchable 50+ physics constants with one-tap insertion into the main calculator, and an interactive 64-bit/32-bit/16-bit/8-bit binary bit-flipper grid.
- **Statistics, Distributions & Finance**: Dynamic chip-based dataset entry with instant descriptive metrics, wavy progress indicator bars for probability distributions (`Normal`, `Binomial`, `Poisson`), and interactive sliders/fields for Loan Amortization and Compound Interest.
