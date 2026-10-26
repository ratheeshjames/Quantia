package com.example.domain

import kotlin.math.abs

data class UnitItem(
    val symbol: String,
    val name: String,
    val toBaseFactor: Double,
    val offsetToC: Double = 0.0 // Reserved for temperature special handling
)

data class UnitCategory(
    val id: String,
    val title: String,
    val baseUnitSymbol: String,
    val units: List<UnitItem>
)

data class PhysicsConstant(
    val symbol: String,
    val name: String,
    val valueString: String,
    val numericValue: Double,
    val unit: String,
    val category: String
)

enum class WordSize(val bits: Int, val label: String) {
    BIT_64(64, "QWORD 64-bit"),
    BIT_32(32, "DWORD 32-bit"),
    BIT_16(16, "WORD 16-bit"),
    BIT_8(8, "BYTE 8-bit")
}

object CatalogAndConverterEngine {

    // 30 Comprehensive Unit Categories
    val unitCategories: List<UnitCategory> = listOf(
        UnitCategory("length", "Length", "m", listOf(
            UnitItem("m", "Meter", 1.0),
            UnitItem("km", "Kilometer", 1000.0),
            UnitItem("cm", "Centimeter", 0.01),
            UnitItem("mm", "Millimeter", 0.001),
            UnitItem("μm", "Micrometer", 1e-6),
            UnitItem("nm", "Nanometer", 1e-9),
            UnitItem("mi", "Mile", 1609.344),
            UnitItem("yd", "Yard", 0.9144),
            UnitItem("ft", "Foot", 0.3048),
            UnitItem("in", "Inch", 0.0254),
            UnitItem("nmi", "Nautical Mile", 1852.0),
            UnitItem("AU", "Astronomical Unit", 1.495978707e11),
            UnitItem("ly", "Light-Year", 9.4607304725808e15)
        )),
        UnitCategory("mass", "Mass & Weight", "kg", listOf(
            UnitItem("kg", "Kilogram", 1.0),
            UnitItem("g", "Gram", 0.001),
            UnitItem("mg", "Milligram", 1e-6),
            UnitItem("μg", "Microgram", 1e-9),
            UnitItem("t", "Metric Tonne", 1000.0),
            UnitItem("lb", "Pound", 0.45359237),
            UnitItem("oz", "Ounce", 0.028349523125),
            UnitItem("st", "Stone", 6.35029318),
            UnitItem("ct", "Carat", 0.0002),
            UnitItem("u", "Atomic Mass Unit", 1.6605390666e-27)
        )),
        UnitCategory("temperature", "Temperature", "°C", listOf(
            UnitItem("°C", "Celsius", 1.0),
            UnitItem("°F", "Fahrenheit", 1.0),
            UnitItem("K", "Kelvin", 1.0),
            UnitItem("°R", "Rankine", 1.0)
        )),
        UnitCategory("area", "Area", "m²", listOf(
            UnitItem("m²", "Square Meter", 1.0),
            UnitItem("km²", "Square Kilometer", 1e6),
            UnitItem("cm²", "Square Centimeter", 1e-4),
            UnitItem("ha", "Hectare", 10000.0),
            UnitItem("ac", "Acre", 4046.8564224),
            UnitItem("ft²", "Square Foot", 0.09290304),
            UnitItem("in²", "Square Inch", 0.00064516),
            UnitItem("mi²", "Square Mile", 2589988.110336)
        )),
        UnitCategory("volume", "Volume", "L", listOf(
            UnitItem("L", "Liter", 1.0),
            UnitItem("mL", "Milliliter", 0.001),
            UnitItem("m³", "Cubic Meter", 1000.0),
            UnitItem("cm³", "Cubic Centimeter", 0.001),
            UnitItem("gal", "US Gallon", 3.785411784),
            UnitItem("qt", "US Quart", 0.946352946),
            UnitItem("pt", "US Pint", 0.473176473),
            UnitItem("fl oz", "US Fluid Ounce", 0.02957352956),
            UnitItem("bbl", "Oil Barrel", 158.987294928)
        )),
        UnitCategory("speed", "Speed & Velocity", "m/s", listOf(
            UnitItem("m/s", "Meter / sec", 1.0),
            UnitItem("km/h", "Kilometer / hr", 1.0 / 3.6),
            UnitItem("mph", "Miles / hr", 0.44704),
            UnitItem("kn", "Knot", 0.514444444),
            UnitItem("ft/s", "Foot / sec", 0.3048),
            UnitItem("Mach", "Mach (Sea Level)", 340.29),
            UnitItem("c", "Speed of Light", 299792458.0)
        )),
        UnitCategory("time", "Time", "s", listOf(
            UnitItem("s", "Second", 1.0),
            UnitItem("ms", "Millisecond", 1e-3),
            UnitItem("μs", "Microsecond", 1e-6),
            UnitItem("ns", "Nanosecond", 1e-9),
            UnitItem("min", "Minute", 60.0),
            UnitItem("h", "Hour", 3600.0),
            UnitItem("d", "Day", 86400.0),
            UnitItem("wk", "Week", 604800.0),
            UnitItem("yr", "Julian Year", 31557600.0)
        )),
        UnitCategory("storage", "Digital Storage", "B", listOf(
            UnitItem("B", "Byte", 1.0),
            UnitItem("bit", "Bit", 0.125),
            UnitItem("KB", "Kilobyte (10³)", 1e3),
            UnitItem("KiB", "Kibibyte (2¹⁰)", 1024.0),
            UnitItem("MB", "Megabyte (10⁶)", 1e6),
            UnitItem("MiB", "Mebibyte (2²⁰)", 1048576.0),
            UnitItem("GB", "Gigabyte (10⁹)", 1e9),
            UnitItem("GiB", "Gibibyte (2³⁰)", 1073741824.0),
            UnitItem("TB", "Terabyte (10¹²)", 1e12),
            UnitItem("PB", "Petabyte (10¹⁵)", 1e15)
        )),
        UnitCategory("datarate", "Data Transfer Rate", "Mbps", listOf(
            UnitItem("Mbps", "Megabits / sec", 1.0),
            UnitItem("Kbps", "Kilobits / sec", 0.001),
            UnitItem("Gbps", "Gigabits / sec", 1000.0),
            UnitItem("MB/s", "Megabytes / sec", 8.0),
            UnitItem("GB/s", "Gigabytes / sec", 8000.0)
        )),
        UnitCategory("energy", "Energy & Work", "J", listOf(
            UnitItem("J", "Joule", 1.0),
            UnitItem("kJ", "Kilojoule", 1000.0),
            UnitItem("cal", "Calorie (therm)", 4.184),
            UnitItem("kcal", "Kilocalorie (Food)", 4184.0),
            UnitItem("Wh", "Watt-hour", 3600.0),
            UnitItem("kWh", "Kilowatt-hour", 3.6e6),
            UnitItem("eV", "Electronvolt", 1.602176634e-19),
            UnitItem("BTU", "British Thermal Unit", 1055.05585)
        )),
        UnitCategory("power", "Power", "W", listOf(
            UnitItem("W", "Watt", 1.0),
            UnitItem("mW", "Milliwatt", 0.001),
            UnitItem("kW", "Kilowatt", 1000.0),
            UnitItem("MW", "Megawatt", 1e6),
            UnitItem("hp", "Mechanical Horsepower", 745.699872),
            UnitItem("PS", "Metric Horsepower", 735.49875)
        )),
        UnitCategory("pressure", "Pressure & Stress", "Pa", listOf(
            UnitItem("Pa", "Pascal", 1.0),
            UnitItem("kPa", "Kilopascal", 1000.0),
            UnitItem("MPa", "Megapascal", 1e6),
            UnitItem("bar", "Bar", 1e5),
            UnitItem("mbar", "Millibar / hPa", 100.0),
            UnitItem("atm", "Standard Atmosphere", 101325.0),
            UnitItem("psi", "Pound / sq inch", 6894.75729),
            UnitItem("mmHg", "Torr / mmHg", 133.322387415)
        )),
        UnitCategory("force", "Force", "N", listOf(
            UnitItem("N", "Newton", 1.0),
            UnitItem("kN", "Kilonewton", 1000.0),
            UnitItem("dyn", "Dyne", 1e-5),
            UnitItem("lbf", "Pound-force", 4.4482216152605),
            UnitItem("kgf", "Kilogram-force", 9.80665)
        )),
        UnitCategory("angle", "Plane Angle", "deg", listOf(
            UnitItem("deg", "Degree (°)", 1.0),
            UnitItem("rad", "Radian", 180.0 / Math.PI),
            UnitItem("grad", "Gradian (gon)", 0.9),
            UnitItem("arcmin", "Arcminute (′)", 1.0 / 60.0),
            UnitItem("arcsec", "Arcsecond (″)", 1.0 / 3600.0),
            UnitItem("rev", "Turn / Revolution", 360.0)
        )),
        UnitCategory("frequency", "Frequency", "Hz", listOf(
            UnitItem("Hz", "Hertz", 1.0),
            UnitItem("kHz", "Kilohertz", 1e3),
            UnitItem("MHz", "Megahertz", 1e6),
            UnitItem("GHz", "Gigahertz", 1e9),
            UnitItem("THz", "Terahertz", 1e12),
            UnitItem("RPM", "Revolutions / min", 1.0 / 60.0)
        )),
        UnitCategory("fuel", "Fuel Economy", "km/L", listOf(
            UnitItem("km/L", "Kilometers / Liter", 1.0),
            UnitItem("mpg US", "MPG (US)", 0.425143707),
            UnitItem("mpg UK", "MPG (Imperial)", 0.35400619)
        )),
        UnitCategory("torque", "Torque", "N·m", listOf(
            UnitItem("N·m", "Newton-meter", 1.0),
            UnitItem("lb·ft", "Pound-foot", 1.355817948),
            UnitItem("lb·in", "Pound-inch", 0.112984829),
            UnitItem("kgf·m", "Kilogram-force meter", 9.80665)
        )),
        UnitCategory("density", "Density", "kg/m³", listOf(
            UnitItem("kg/m³", "kg / cubic meter", 1.0),
            UnitItem("g/cm³", "g / cubic cm", 1000.0),
            UnitItem("lb/ft³", "lb / cubic foot", 16.01846337),
            UnitItem("lb/gal", "lb / US gallon", 119.826427)
        )),
        UnitCategory("dyn_visc", "Dynamic Viscosity", "Pa·s", listOf(
            UnitItem("Pa·s", "Pascal-second", 1.0),
            UnitItem("mPa·s", "Millipascal-s / cP", 0.001),
            UnitItem("P", "Poise", 0.1)
        )),
        UnitCategory("kin_visc", "Kinematic Viscosity", "m²/s", listOf(
            UnitItem("m²/s", "Sq meter / sec", 1.0),
            UnitItem("St", "Stokes", 1e-4),
            UnitItem("cSt", "Centistokes", 1e-6)
        )),
        UnitCategory("current", "Electric Current", "A", listOf(
            UnitItem("A", "Ampere", 1.0),
            UnitItem("mA", "Milliampere", 1e-3),
            UnitItem("μA", "Microampere", 1e-6),
            UnitItem("kA", "Kiloampere", 1000.0)
        )),
        UnitCategory("voltage", "Electric Potential", "V", listOf(
            UnitItem("V", "Volt", 1.0),
            UnitItem("mV", "Millivolt", 1e-3),
            UnitItem("μV", "Microvolt", 1e-6),
            UnitItem("kV", "Kilovolt", 1000.0),
            UnitItem("MV", "Megavolt", 1e6)
        )),
        UnitCategory("resistance", "Electric Resistance", "Ω", listOf(
            UnitItem("Ω", "Ohm", 1.0),
            UnitItem("mΩ", "Milliohm", 1e-3),
            UnitItem("kΩ", "Kiloohm", 1e3),
            UnitItem("MΩ", "Megaohm", 1e6)
        )),
        UnitCategory("capacitance", "Capacitance", "F", listOf(
            UnitItem("F", "Farad", 1.0),
            UnitItem("mF", "Millifarad", 1e-3),
            UnitItem("μF", "Microfarad", 1e-6),
            UnitItem("nF", "Nanofarad", 1e-9),
            UnitItem("pF", "Picofarad", 1e-12)
        )),
        UnitCategory("inductance", "Inductance", "H", listOf(
            UnitItem("H", "Henry", 1.0),
            UnitItem("mH", "Millihenry", 1e-3),
            UnitItem("μH", "Microhenry", 1e-6),
            UnitItem("nH", "Nanohenry", 1e-9)
        )),
        UnitCategory("mag_flux", "Magnetic Flux Density", "T", listOf(
            UnitItem("T", "Tesla", 1.0),
            UnitItem("mT", "Millitesla", 1e-3),
            UnitItem("μT", "Microtesla", 1e-6),
            UnitItem("G", "Gauss", 1e-4)
        )),
        UnitCategory("illuminance", "Illuminance", "lx", listOf(
            UnitItem("lx", "Lux", 1.0),
            UnitItem("fc", "Foot-candle", 10.7639104),
            UnitItem("ph", "Phot", 10000.0)
        )),
        UnitCategory("radiation", "Radiation Dose", "Sv", listOf(
            UnitItem("Sv", "Sievert", 1.0),
            UnitItem("mSv", "Millisievert", 1e-3),
            UnitItem("μSv", "Microsievert", 1e-6),
            UnitItem("rem", "Roentgen eq. man", 0.01)
        )),
        UnitCategory("typography", "Typography & CSS", "px", listOf(
            UnitItem("px", "CSS Pixel (96 DPI)", 1.0),
            UnitItem("pt", "Point (1/72 in)", 96.0 / 72.0),
            UnitItem("pc", "Pica (12 pt)", 16.0),
            UnitItem("em", "em / rem (16px base)", 16.0),
            UnitItem("in", "Inch", 96.0),
            UnitItem("mm", "Millimeter", 96.0 / 25.4)
        )),
        UnitCategory("cooking", "Culinary & Cooking", "cup", listOf(
            UnitItem("cup", "US Cup (240 mL)", 1.0),
            UnitItem("tbsp", "Tablespoon (15 mL)", 1.0 / 16.0),
            UnitItem("tsp", "Teaspoon (5 mL)", 1.0 / 48.0),
            UnitItem("mL", "Milliliter", 1.0 / 236.5882365),
            UnitItem("fl oz", "Fluid Ounce", 1.0 / 8.0)
        ))
    )

    fun convertUnit(
        category: UnitCategory,
        from: UnitItem,
        to: UnitItem,
        amount: Double
    ): Double {
        if (!amount.isFinite()) return Double.NaN
        if (category.id == "temperature") {
            val celsius = when (from.symbol) {
                "°C" -> amount
                "°F" -> (amount - 32.0) * (5.0 / 9.0)
                "K" -> amount - 273.15
                "°R" -> (amount - 491.67) * (5.0 / 9.0)
                else -> amount
            }
            return when (to.symbol) {
                "°C" -> celsius
                "°F" -> celsius * 1.8 + 32.0
                "K" -> celsius + 273.15
                "°R" -> (celsius + 273.15) * 1.8
                else -> celsius
            }
        }
        val baseValue = amount * from.toBaseFactor
        return baseValue / to.toBaseFactor
    }

    // 52 CODATA Fundamental Physics, Chemistry, Astronomy & Mathematical Constants
    val physicsConstants: List<PhysicsConstant> = listOf(
        // Universal & Quantum (16)
        PhysicsConstant("c", "Speed of Light in Vacuum", "299792458", 299792458.0, "m·s⁻¹", "Universal"),
        PhysicsConstant("h", "Planck Constant", "6.62607015E-34", 6.62607015e-34, "J·s", "Universal"),
        PhysicsConstant("ℏ", "Reduced Planck Constant (h/2π)", "1.054571817E-34", 1.054571817e-34, "J·s", "Universal"),
        PhysicsConstant("G", "Newtonian Gravitational Constant", "6.67430E-11", 6.67430e-11, "m³·kg⁻¹·s⁻²", "Universal"),
        PhysicsConstant("ε₀", "Vacuum Electric Permittivity", "8.8541878128E-12", 8.8541878128e-12, "F·m⁻¹", "Universal"),
        PhysicsConstant("μ₀", "Vacuum Magnetic Permeability", "1.25663706212E-6", 1.25663706212e-6, "N·A⁻²", "Universal"),
        PhysicsConstant("Z₀", "Characteristic Impedance of Vacuum", "376.730313668", 376.730313668, "Ω", "Universal"),
        PhysicsConstant("k_e", "Coulomb Constant (1/4πε₀)", "8.9875517923E9", 8.9875517923e9, "N·m²·C⁻²", "Universal"),
        PhysicsConstant("G₀", "Conductance Quantum", "7.748091729E-5", 7.748091729e-5, "S", "Quantum"),
        PhysicsConstant("Φ₀", "Magnetic Flux Quantum", "2.067833848E-15", 2.067833848e-15, "Wb", "Quantum"),
        PhysicsConstant("K_J", "Josephson Constant", "483597.8484E9", 483597.8484e9, "Hz·V⁻¹", "Quantum"),
        PhysicsConstant("R_K", "von Klitzing Constant", "25812.80745", 25812.80745, "Ω", "Quantum"),
        PhysicsConstant("l_P", "Planck Length", "1.616255E-35", 1.616255e-35, "m", "Quantum"),
        PhysicsConstant("m_P", "Planck Mass", "2.176434E-8", 2.176434e-8, "kg", "Quantum"),
        PhysicsConstant("t_P", "Planck Time", "5.391247E-44", 5.391247e-44, "s", "Quantum"),
        PhysicsConstant("T_P", "Planck Temperature", "1.416784E32", 1.416784e32, "K", "Quantum"),

        // Atomic & Electromagnetic (14)
        PhysicsConstant("e", "Elementary Charge", "1.602176634E-19", 1.602176634e-19, "C", "Atomic"),
        PhysicsConstant("m_e", "Electron Mass", "9.1093837015E-31", 9.1093837015e-31, "kg", "Atomic"),
        PhysicsConstant("m_p", "Proton Mass", "1.67262192369E-27", 1.67262192369e-27, "kg", "Atomic"),
        PhysicsConstant("m_n", "Neutron Mass", "1.67492749804E-27", 1.67492749804e-27, "kg", "Atomic"),
        PhysicsConstant("m_μ", "Muon Mass", "1.883531627E-28", 1.883531627e-28, "kg", "Atomic"),
        PhysicsConstant("α", "Fine-Structure Constant", "0.0072973525693", 0.0072973525693, "dimensionless", "Atomic"),
        PhysicsConstant("R_∞", "Rydberg Constant", "10973731.568160", 10973731.568160, "m⁻¹", "Atomic"),
        PhysicsConstant("a₀", "Bohr Radius", "5.29177210903E-11", 5.29177210903e-11, "m", "Atomic"),
        PhysicsConstant("μ_B", "Bohr Magneton", "9.2740100783E-24", 9.2740100783e-24, "J·T⁻¹", "Atomic"),
        PhysicsConstant("μ_N", "Nuclear Magneton", "5.0507837461E-27", 5.0507837461e-27, "J·T⁻¹", "Atomic"),
        PhysicsConstant("λ_C", "Compton Wavelength of Electron", "2.42631023867E-12", 2.42631023867e-12, "m", "Atomic"),
        PhysicsConstant("r_e", "Classical Electron Radius", "2.8179403262E-15", 2.8179403262e-15, "m", "Atomic"),
        PhysicsConstant("σ_e", "Thomson Cross Section", "6.6524587321E-29", 6.6524587321e-29, "m²", "Atomic"),
        PhysicsConstant("E_h", "Hartree Energy", "4.3597447222071E-18", 4.3597447222071e-18, "J", "Atomic"),

        // Thermodynamics & Chemistry (10)
        PhysicsConstant("N_A", "Avogadro Constant", "6.02214076E23", 6.02214076e23, "mol⁻¹", "Chemistry"),
        PhysicsConstant("k_B", "Boltzmann Constant", "1.380649E-23", 1.380649e-23, "J·K⁻¹", "Thermo"),
        PhysicsConstant("R", "Molar Gas Constant", "8.314462618", 8.314462618, "J·mol⁻¹·K⁻¹", "Thermo"),
        PhysicsConstant("F", "Faraday Constant", "96485.33212", 96485.33212, "C·mol⁻¹", "Chemistry"),
        PhysicsConstant("σ", "Stefan-Boltzmann Constant", "5.670374419E-8", 5.670374419e-8, "W·m⁻²·K⁻⁴", "Thermo"),
        PhysicsConstant("b_W", "Wien Displacement Constant", "0.002897771955", 0.002897771955, "m·K", "Thermo"),
        PhysicsConstant("u", "Unified Atomic Mass Unit", "1.66053906660E-27", 1.66053906660e-27, "kg", "Chemistry"),
        PhysicsConstant("V_m", "Molar Volume of Ideal Gas (STP)", "0.02241396954", 0.02241396954, "m³·mol⁻¹", "Chemistry"),
        PhysicsConstant("c₁", "First Radiation Constant", "3.741771852E-16", 3.741771852e-16, "W·m²", "Thermo"),
        PhysicsConstant("c₂", "Second Radiation Constant", "0.01438776877", 0.01438776877, "m·K", "Thermo"),

        // Astronomy, Earth & Pure Mathematics (12)
        PhysicsConstant("g₀", "Standard Earth Gravity", "9.80665", 9.80665, "m·s⁻²", "Astronomy"),
        PhysicsConstant("atm", "Standard Atmospheric Pressure", "101325", 101325.0, "Pa", "Astronomy"),
        PhysicsConstant("M_☉", "Solar Mass", "1.98847E30", 1.98847e30, "kg", "Astronomy"),
        PhysicsConstant("R_☉", "Solar Radius", "6.957E8", 6.957e8, "m", "Astronomy"),
        PhysicsConstant("M_⊕", "Earth Mass", "5.9722E24", 5.9722e24, "kg", "Astronomy"),
        PhysicsConstant("R_⊕", "Earth Mean Radius", "6371000", 6371000.0, "m", "Astronomy"),
        PhysicsConstant("π", "Archimedes' Constant Pi", "3.141592653589793", Math.PI, "exact", "Math"),
        PhysicsConstant("e_m", "Euler's Number e", "2.718281828459045", Math.E, "exact", "Math"),
        PhysicsConstant("φ", "Golden Ratio Phi", "1.618033988749895", 1.618033988749895, "exact", "Math"),
        PhysicsConstant("γ", "Euler-Mascheroni Constant", "0.5772156649015329", 0.5772156649015329, "exact", "Math"),
        PhysicsConstant("G_c", "Catalan's Constant", "0.915965594177219", 0.915965594177219, "exact", "Math"),
        PhysicsConstant("ζ(3)", "Apéry's Constant", "1.202056903159594", 1.202056903159594, "exact", "Math")
    )

    // Base-N Programmer Bitwise Helper
    fun maskForWordSize(value: Long, wordSize: WordSize): Long {
        return when (wordSize) {
            WordSize.BIT_64 -> value
            WordSize.BIT_32 -> value and 0xFFFFFFFFL
            WordSize.BIT_16 -> value and 0xFFFFL
            WordSize.BIT_8 -> value and 0xFFL
        }
    }

    fun toSignedValue(value: Long, wordSize: WordSize): Long {
        val masked = maskForWordSize(value, wordSize)
        return when (wordSize) {
            WordSize.BIT_64 -> masked
            WordSize.BIT_32 -> masked.toInt().toLong()
            WordSize.BIT_16 -> masked.toShort().toLong()
            WordSize.BIT_8 -> masked.toByte().toLong()
        }
    }

    fun formatHex(value: Long, wordSize: WordSize): String {
        val masked = maskForWordSize(value, wordSize)
        return java.lang.Long.toUnsignedString(masked, 16).uppercase()
    }

    fun formatOct(value: Long, wordSize: WordSize): String {
        val masked = maskForWordSize(value, wordSize)
        return java.lang.Long.toUnsignedString(masked, 8)
    }

    fun formatBin(value: Long, wordSize: WordSize): String {
        val masked = maskForWordSize(value, wordSize)
        val raw = java.lang.Long.toUnsignedString(masked, 2)
        return raw.padStart(minOf(wordSize.bits, 32), '0').chunked(4).joinToString(" ")
    }

    fun applyBitwiseOp(a: Long, b: Long, op: String, wordSize: WordSize): Long {
        val ma = maskForWordSize(a, wordSize)
        val mb = maskForWordSize(b, wordSize)
        val shift = (mb.toInt() and (wordSize.bits - 1))
        val res = when (op) {
            "AND" -> ma and mb
            "OR" -> ma or mb
            "XOR" -> ma xor mb
            "NAND" -> (ma and mb).inv()
            "NOR" -> (ma or mb).inv()
            "NOT" -> ma.inv()
            "SHL" -> ma shl shift
            "SHR" -> ma ushr shift
            "ROL" -> (ma shl shift) or (ma ushr (wordSize.bits - shift))
            "ROR" -> (ma ushr shift) or (ma shl (wordSize.bits - shift))
            else -> ma
        }
        return maskForWordSize(res, wordSize)
    }
}
