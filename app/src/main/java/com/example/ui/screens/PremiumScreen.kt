package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import android.widget.Toast
import coil.compose.AsyncImage
import java.net.URLEncoder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.ui.theme.ChatGptGreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.PremiumGold
import com.example.ui.theme.PremiumGoldLight
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextWhite
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PremiumScreen(
    isPremium: Boolean,
    onUpgradeSuccess: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedPlanIndex by remember { mutableIntStateOf(1) } // Default to Monthly (Index 1)
    var isProcessingPayment by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    
    // UPI payment states
    var showUpiSection by remember { mutableStateOf(true) }
    var utrNumber by remember { mutableStateOf("") }
    var utrError by remember { mutableStateOf<String?>(null) }

    // Festival Offer Coupon states
    var couponCode by remember { mutableStateOf("") }
    var discountApplied by remember { mutableStateOf(false) }
    var couponError by remember { mutableStateOf<String?>(null) }
    
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val prefs = remember { com.example.data.pref.PreferencesManager(context) }
    var isFestivalActive by remember { mutableStateOf(prefs.isFestivalActive) }

    // White / Light Mode Theme Colors
    val bgMain = Color(0xFFF8FAFC) // Light slate gray background
    val surfaceColor = Color.White
    val textPrimary = Color(0xFF0F172A) // Near-black dark slate text
    val textSecondary = Color(0xFF475569) // Muted slate gray text
    val accentGold = Color(0xFFD97706) // Rich elegant gold/amber accent (light-mode friendly)
    val accentGoldLight = Color(0xFFFEF3C7) // Soft background gold accent
    val borderLight = Color(0xFFE2E8F0) // Subtle card & field borders
    val errorColor = Color(0xFFDC2626) // Vivid red error
    val successColor = Color(0xFF16A34A) // Beautiful M3 emerald green

    val plans = if (discountApplied && isFestivalActive) {
        listOf(
            PremiumPlan("Weekly", "₹74", "per week", false, "FESTIVAL OFFER • 50% Off"),
            PremiumPlan("Monthly", "₹199", "per month", true, "FESTIVAL SPECIAL • Save 80%!"),
            PremiumPlan("1 Year", "₹999", "per year", false, "FESTIVAL SPECIAL • Save 50%!")
        )
    } else {
        listOf(
            PremiumPlan("Weekly", "₹149", "per week", false, "Limited Access"),
            PremiumPlan("Monthly", "₹399", "per month", true, "Best Value • Save 60%"),
            PremiumPlan("1 Year", "₹1999", "per year", false, "Ultimate Access")
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgMain)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Hero Header with generated img_premium_paywall background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_premium_paywall),
                    contentDescription = "Premium Paywall Banner",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Light scrim overlay with gradient
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    bgMain.copy(alpha = 0.4f),
                                    bgMain
                                )
                            )
                        )
                )

                // Top Toolbar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .testTag("close_premium_button")
                            .background(Color.Black.copy(alpha = 0.1f), RoundedCornerShape(50))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close paywall",
                            tint = textPrimary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(
                                brush = Brush.horizontalGradient(listOf(accentGold, Color(0xFFF59E0B))),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "PRO ACCESS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    letterSpacing = 1.sp
                                )
                            )
                        }
                    }
                }

                // Centered Hero Title inside Banner
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Nova AI Premium",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = textPrimary,
                            textAlign = TextAlign.Center
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Get ChatGPT Plus tier logic at an affordable price",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = textSecondary,
                            textAlign = TextAlign.Center
                        )
                    )
                }
            }

            // Paywall Features List (Dynamic based on package type)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (selectedPlanIndex == 0) {
                    // Weekly Package: Limited Access
                    Text(
                        text = "WEEKLY PLAN FEATURES (LIMITED ACCESS)",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = errorColor,
                            letterSpacing = 1.sp
                        )
                    )
                    PremiumFeatureItem(
                        icon = Icons.Default.Close,
                        iconColor = errorColor,
                        title = "Limited Chat Allocation",
                        description = "Capped speed & standard daily conversation limits.",
                        cardBg = surfaceColor,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        borderCol = borderLight
                    )
                    PremiumFeatureItem(
                        icon = Icons.Default.Close,
                        iconColor = errorColor,
                        title = "No Pro Personalities",
                        description = "Premium Coder, Creative Writer, and Zen Life Coach are locked.",
                        cardBg = surfaceColor,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        borderCol = borderLight
                    )
                } else {
                    // Extended Packages: Monthly & 1 Year
                    Text(
                        text = "EXTENDED PREMIUM FEATURES (UNLIMITED ACCESS)",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = successColor,
                            letterSpacing = 1.sp
                        )
                    )
                    PremiumFeatureItem(
                        icon = Icons.Default.Star,
                        iconColor = accentGold,
                        title = "Unlimited High-Speed Chats",
                        description = "Completely remove the daily message limits and latency queues.",
                        cardBg = surfaceColor,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        borderCol = borderLight
                    )
                    PremiumFeatureItem(
                        icon = Icons.Default.Star,
                        iconColor = successColor,
                        title = "Advanced Reasoning Engine",
                        description = "Unlocks 'gemini-3.1-pro-preview' for advanced coding, complex reasoning, and long texts.",
                        cardBg = surfaceColor,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        borderCol = borderLight
                    )
                    PremiumFeatureItem(
                        icon = Icons.Default.Lock,
                        iconColor = Color(0xFF4F46E5), // Deep beautiful indigo
                        title = "Premium AI Personas Unlocked",
                        description = "Gain unlimited access to Coder, Creative Writer, and Zen Coach.",
                        cardBg = surfaceColor,
                        textPrimary = textPrimary,
                        textSecondary = textSecondary,
                        borderCol = borderLight
                    )
                }
            }

            // Festival Offer Promo Banner & Coupon Card (ONLY visible when isFestivalActive is true!)
            if (isFestivalActive) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .border(1.dp, accentGold.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = accentGoldLight.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "🎉 FESTIVAL CELEBRATION OFFER! 🎉",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = accentGold,
                                    fontSize = 15.sp
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Celebrate this festive season with a flat 50% off on all pro subscriptions. Type coupon FESTIVAL50 to activate instantly!",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = textPrimary,
                                textAlign = TextAlign.Center,
                                lineHeight = 16.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = couponCode,
                                onValueChange = {
                                    couponCode = it
                                    couponError = null
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                                    .testTag("coupon_input_field"),
                                placeholder = { Text("Enter Coupon (FESTIVAL50)", color = textSecondary.copy(alpha = 0.5f), fontSize = 12.sp) },
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = accentGold,
                                    unfocusedBorderColor = borderLight,
                                    focusedLabelColor = accentGold,
                                    unfocusedLabelColor = textSecondary,
                                    focusedTextColor = textPrimary,
                                    unfocusedTextColor = textPrimary
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (couponCode.trim().uppercase() == "FESTIVAL50") {
                                        discountApplied = true
                                        couponError = null
                                        Toast.makeText(context, "Festival Offer Coupon Applied! 50% discount unlocked!", Toast.LENGTH_LONG).show()
                                    } else {
                                        couponError = "Invalid coupon code."
                                        Toast.makeText(context, "Invalid coupon code. Try FESTIVAL50", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier
                                    .height(46.dp)
                                    .testTag("apply_coupon_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = accentGold),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Apply", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                        if (discountApplied) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "✓ Coupon 'FESTIVAL50' applied successfully! Prices updated.",
                                color = successColor,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Plan Selector Title
            Text(
                text = "CHOOSE A PLAN",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = textSecondary,
                    letterSpacing = 1.5.sp
                ),
                modifier = Modifier.padding(start = 20.dp, top = 16.dp, bottom = 8.dp)
            )

            // Selectable Plan Cards
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                plans.forEachIndexed { index, plan ->
                    val isSelected = selectedPlanIndex == index
                    val borderBrush = if (isSelected) {
                        Brush.horizontalGradient(listOf(accentGold, Color(0xFFF59E0B)))
                    } else {
                        Brush.linearGradient(listOf(borderLight, borderLight))
                    }

                    val cardBackground = if (isSelected) accentGoldLight.copy(alpha = 0.25f) else surfaceColor

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                brush = borderBrush,
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { selectedPlanIndex = index }
                            .testTag("premium_plan_card_$index"),
                        colors = CardDefaults.cardColors(containerColor = cardBackground)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Custom Radio Indicator
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .border(
                                                width = 2.dp,
                                                color = if (isSelected) accentGold else textSecondary.copy(alpha = 0.5f),
                                                shape = RoundedCornerShape(50)
                                            )
                                            .padding(4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(accentGold, RoundedCornerShape(50))
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = plan.title,
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = textPrimary
                                                )
                                            )
                                            plan.badge?.let { badge ->
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .background(
                                                            if (plan.isHighlighted) successColor else Color.Black.copy(alpha = 0.08f),
                                                            RoundedCornerShape(4.dp)
                                                        )
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(
                                                        text = badge,
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (plan.isHighlighted) Color.White else textSecondary
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            text = plan.subtitle,
                                            style = MaterialTheme.typography.bodySmall.copy(color = textSecondary)
                                        )
                                    }
                                }

                                Text(
                                    text = plan.price,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isSelected) accentGold else textPrimary
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Subscribe Button
            Button(
                onClick = {
                    if (!isPremium) {
                        showUpiSection = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .padding(horizontal = 16.dp)
                    .testTag("subscribe_action_button"),
                enabled = !isPremium && !isProcessingPayment,
                colors = ButtonDefaults.buttonColors(
                    containerColor = successColor,
                    disabledContainerColor = successColor.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isProcessingPayment) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isPremium) "Premium Active" else "Subscribe Now for ${plans[selectedPlanIndex].price}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                }
            }

            // Animated UPI Payment Section
            AnimatedVisibility(visible = showUpiSection && !isPremium) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .border(1.dp, accentGold.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = surfaceColor),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = accentGold,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "UPI Secure Checkout",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = accentGold
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Scan with GPay, PhonePe, Paytm, or BHIM to pay " + plans[selectedPlanIndex].price,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = textSecondary,
                                textAlign = TextAlign.Center
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // QR Code generation via QR Server API using UPI URI scheme (High-compatibility format for personal VPAs)
                        val rawUpiUri = "upi://pay?pa=7780759035@ybl&pn=SANIYA%20SULTANA"
                        val qrCodeUrl = "https://api.qrserver.com/v1/create-qr-code/?size=300x300&margin=10&data=" + URLEncoder.encode(rawUpiUri, "UTF-8")

                        Box(
                            modifier = Modifier
                                .size(180.dp)
                                .background(Color.White, RoundedCornerShape(12.dp))
                                .border(1.dp, accentGold.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = qrCodeUrl,
                                contentDescription = "UPI Payment QR Code",
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "💡 Please enter the amount manually in your payment app after scanning.",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = accentGold,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // UPI ID copy box (Saves VPA privately, hides the raw phone number)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFF1F5F9))
                                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(10.dp))
                                .clickable {
                                    clipboardManager.setText(AnnotatedString("7780759035@ybl"))
                                    Toast.makeText(context, "UPI Address copied to clipboard!", Toast.LENGTH_SHORT).show()
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "SECURE PAYMENT ID",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = textSecondary,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "Tap to Copy Payment Address",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = accentGold,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Copy",
                                tint = accentGold,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Ref/UTR number text field
                        OutlinedTextField(
                            value = utrNumber,
                            onValueChange = {
                                utrNumber = it
                                utrError = null
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("utr_input_field"),
                            label = { Text("Transaction Reference / UTR No.", color = textSecondary) },
                            placeholder = { Text("e.g. 301294819234", color = textSecondary.copy(alpha = 0.4f)) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = accentGold,
                                unfocusedBorderColor = borderLight,
                                focusedLabelColor = accentGold,
                                unfocusedLabelColor = textSecondary,
                                focusedTextColor = textPrimary,
                                unfocusedTextColor = textPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        if (utrError != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = utrError ?: "",
                                color = errorColor,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Start
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Submit Button
                        Button(
                            onClick = {
                                if (utrNumber.trim().length < 8) {
                                    utrError = "Please enter a valid Transaction UTR / Ref Number."
                                } else {
                                    isProcessingPayment = true
                                    scope.launch {
                                        delay(1500)
                                        isProcessingPayment = false
                                        showSuccessDialog = true
                                        showUpiSection = false
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("verify_upi_payment_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = accentGold),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "Submit & Verify Payment",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Subscriptions T&C
            Text(
                text = "Cancel subscription anytime in Google Play. Terms & Privacy Policy apply.",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = textSecondary.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 🧪 Developer / Admin Testing Tools
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                border = BorderStroke(1.dp, borderLight),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🧪 DEV CONTROLLERS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = textSecondary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Simulate festival periods to show/hide the coupon input dynamically.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = textSecondary,
                            textAlign = TextAlign.Center
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            val newStatus = !isFestivalActive
                            prefs.isFestivalActive = newStatus
                            isFestivalActive = newStatus
                            if (newStatus) {
                                Toast.makeText(context, "Festival Mode Activated! Coupon option is now visible.", Toast.LENGTH_SHORT).show()
                            } else {
                                discountApplied = false
                                Toast.makeText(context, "Festival Mode Deactivated! Coupon option is now hidden.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (isFestivalActive) Color(0xFFEF4444) else successColor),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (isFestivalActive) "Toggle Festival Mode (Current: ${if (isFestivalActive) "ACTIVE" else "INACTIVE"})" else "Activate Festival Mode",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        // Celebrate purchase dialog
        if (showSuccessDialog) {
            Dialog(
                onDismissRequest = {
                    showSuccessDialog = false
                    onUpgradeSuccess()
                },
                properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = surfaceColor,
                    tonalElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Glowing Golden Circle
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .background(accentGold.copy(alpha = 0.15f), RoundedCornerShape(50)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Success",
                                tint = accentGold,
                                modifier = Modifier.size(48.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Purchase Successful!",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = textPrimary,
                                textAlign = TextAlign.Center
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "You are now a Nova AI Pro member. Enjoy unlimited fast messages, expert personas, and the intelligent smart logic engine!",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = textSecondary,
                                textAlign = TextAlign.Center
                            )
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                showSuccessDialog = false
                                onUpgradeSuccess()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = accentGold),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("continue_to_chat_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Start Chatting",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PremiumFeatureItem(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    description: String,
    cardBg: Color,
    textPrimary: Color,
    textSecondary: Color,
    borderCol: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(cardBg, RoundedCornerShape(12.dp))
            .border(1.dp, borderCol, RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(iconColor.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = textPrimary
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall.copy(color = textSecondary)
            )
        }
    }
}

data class PremiumPlan(
    val title: String,
    val price: String,
    val subtitle: String,
    val isHighlighted: Boolean,
    val badge: String?
)
