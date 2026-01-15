package com.ipca.lojasocial.presentation.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * SHAPES MODERNOS - Cantos mais suaves e consistentes
 * 
 * Material Design 3 recomenda:
 * - Extra Small: 4dp
 * - Small: 8dp
 * - Medium: 12dp
 * - Large: 16dp
 * - Extra Large: 28dp
 */
val Shapes = Shapes(
    // Extra Small - Chips pequenos, badges
    extraSmall = RoundedCornerShape(4.dp),
    
    // Small - Botões pequenos, switches
    small = RoundedCornerShape(8.dp),
    
    // Medium - Cards, botões normais (PADRÃO)
    medium = RoundedCornerShape(12.dp),  // Mais suave que 8dp
    
    // Large - Dialogs, bottom sheets
    large = RoundedCornerShape(16.dp),
    
    // Extra Large - FABs, grandes cards
    extraLarge = RoundedCornerShape(28.dp)
)

/**
 * EXTENSÕES ÚTEIS PARA CASOS ESPECIAIS
 */

// Sem cantos arredondados (para casos específicos)
val ShapeNone = RoundedCornerShape(0.dp)

// Cantos superiores arredondados (bottom sheets, modals)
val ShapeTopRounded = RoundedCornerShape(
    topStart = 16.dp,
    topEnd = 16.dp,
    bottomStart = 0.dp,
    bottomEnd = 0.dp
)

// Cantos inferiores arredondados (headers fixos)
val ShapeBottomRounded = RoundedCornerShape(
    topStart = 0.dp,
    topEnd = 0.dp,
    bottomStart = 16.dp,
    bottomEnd = 16.dp
)

// Pill shape (totalmente arredondado)
val ShapePill = RoundedCornerShape(50)

// Card destacado (mais arredondado)
val ShapeCardHighlight = RoundedCornerShape(20.dp)

/**
 * NOTAS:
 * - 12dp para cards é mais moderno que 8dp padrão
 * - 16dp para dialogs dá sensação de leveza
 * - 28dp para FABs segue Material Design 3
 * - Consistência: usar sempre os mesmos valores
 */
