package com.embien.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


data class CardData(
    val id: Int,
    val holderName: String,
    val number: String,
    val expiry: String,
    val balance: String,
    val network: String,
    val gradientColors: List<Color>,
    val accentColor: Color
)

data class Transaction(
    val id: Int,
    val title: String,
    val subtitle: String,
    val amount: String,
    val isCredit: Boolean,
    val icon: String,
    val date: String,
    val category: String
)

data class SpendingCategory(
    val label: String,
    val percent: Float,
    val color: Color,
    val amount: String
)


class FlipCardWithChartActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { NexusWalletApp() }
    }
}


val BgDeep      = Color(0xFF060914)
val BgCard      = Color(0xFF0E1525)
val BgSurface   = Color(0xFF141D2E)
val Accent      = Color(0xFF4F8EF7)
val AccentGreen = Color(0xFF22C55E)
val AccentRed   = Color(0xFFEF4444)
val TextPrim    = Color(0xFFEDF2FF)
val TextSub     = Color(0xFF8899BB)
val TextDim     = Color(0xFF3D5070)
val DividerLine = Color(0xFF1C2A40)

val EaseInOutSine = CubicBezierEasing(0.37f, 0f, 0.63f, 1f)


val cards = listOf(
    CardData(1, "AADHI S", "**** **** **** 3456", "12/28", "₹1,25,000", "VISA",
        listOf(Color(0xFF4F8EF7), Color(0xFF7C3AED), Color(0xFFEC4899)), Color(0xFF4F8EF7)),
    CardData(2, "AADHI S", "**** **** **** 7821", "09/27", "₹68,500", "MASTERCARD",
        listOf(Color(0xFF10B981), Color(0xFF0D9488), Color(0xFF0EA5E9)), Color(0xFF10B981)),
    CardData(3, "AADHI S", "**** **** **** 5190", "03/26", "₹2,10,000", "RUPAY",
        listOf(Color(0xFFF59E0B), Color(0xFFEF4444), Color(0xFFEC4899)), Color(0xFFF59E0B))
)

val transactions = listOf(
    Transaction(1, "Amazon", "Shopping · Electronics", "-₹2,500", false, "🛒", "Today", "Shopping"),
    Transaction(2, "Salary Credit", "Embien Technologies", "+₹50,000", true, "💼", "Today", "Income"),
    Transaction(3, "Swiggy", "Food & Drinks", "-₹450", false, "🍔", "Yesterday", "Food"),
    Transaction(4, "Netflix", "Subscription", "-₹799", false, "🎬", "Yesterday", "Entertainment"),
    Transaction(5, "PhonePe", "UPI Transfer", "+₹3,000", true, "📲", "Mon", "Transfer"),
    Transaction(6, "Spotify", "Music Premium", "-₹119", false, "🎵", "Mon", "Entertainment"),
    Transaction(7, "IRCTC", "Train Booking", "-₹1,240", false, "🚂", "Sun", "Travel"),
    Transaction(8, "Freelance", "Design Project", "+₹12,000", true, "🎨", "Sun", "Income"),
)

val spendingData = listOf(
    SpendingCategory("Shopping", 0.38f, Color(0xFF4F8EF7), "₹9,400"),
    SpendingCategory("Food", 0.24f, Color(0xFF10B981), "₹5,950"),
    SpendingCategory("Travel", 0.18f, Color(0xFFF59E0B), "₹4,460"),
    SpendingCategory("Entertainment", 0.12f, Color(0xFFEC4899), "₹2,974"),
    SpendingCategory("Others", 0.08f, Color(0xFF8B5CF6), "₹1,982"),
)

// ─── Root App ─────────────────────────────────────────────────────────────────

@Composable
fun NexusWalletApp() {
    var selectedTab by remember { mutableStateOf(0) }
    var showSendSheet by remember { mutableStateOf(false) }
    var showNotifications by remember { mutableStateOf(false) }
    var showSpendingDialog by remember { mutableStateOf(false) }


    Surface(modifier = Modifier.fillMaxSize(), color = BgDeep) {
        Box(modifier = Modifier.fillMaxSize()) {
            AmbientBackground()

            LazyColumn(
                modifier = Modifier.fillMaxSize().navigationBarsPadding(),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                item { TopBar(onNotifClick = { showNotifications = !showNotifications }) }
                item { Spacer(Modifier.height(8.dp)) }
                item { CardPagerSection() }
                item { Spacer(Modifier.height(24.dp)) }
                item { QuickStatsRow() }
                item { Spacer(Modifier.height(24.dp)) }
                item { ActionGrid(onSend = { showSendSheet = true }) }
                item { Spacer(Modifier.height(24.dp)) }
                item {
                    Box(modifier = Modifier.clickable { showSpendingDialog = true }) {
                        SpendingChart()
                    }
                }
                item { Spacer(Modifier.height(24.dp)) }
                item { TransactionSection() }
            }

            BottomNavBar(
                selected = selectedTab,
                onSelect = { selectedTab = it },
                modifier = Modifier.align(Alignment.BottomCenter)
            )

            AnimatedVisibility(
                visible = showNotifications,
                enter = fadeIn() + slideInVertically { -40 },
                exit = fadeOut() + slideOutVertically { -40 },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                NotificationPanel(onDismiss = { showNotifications = false })
            }

            if (showSendSheet) {
                SendMoneySheet(onDismiss = { showSendSheet = false })
            }
            if (showSpendingDialog) {
                SpendingChartDialog(onDismiss = { showSpendingDialog = false })
            }
        }
    }
}

@Composable
fun AmbientBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawCircle(
            brush = Brush.radialGradient(
                listOf(Color(0xFF1A2B6D).copy(alpha = 0.5f), Color.Transparent),
                center = Offset(size.width * 0.8f, size.height * 0.15f),
                radius = size.width * 0.6f
            ),
            radius = size.width * 0.6f,
            center = Offset(size.width * 0.8f, size.height * 0.15f)
        )
        drawCircle(
            brush = Brush.radialGradient(
                listOf(Color(0xFF2D1B4E).copy(alpha = 0.35f), Color.Transparent),
                center = Offset(size.width * 0.1f, size.height * 0.6f),
                radius = size.width * 0.5f
            ),
            radius = size.width * 0.5f,
            center = Offset(size.width * 0.1f, size.height * 0.6f)
        )
    }
}

// ─── Top Bar ──────────────────────────────────────────────────────────────────

@Composable
fun TopBar(onNotifClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("Good Morning 👋", color = TextSub, fontSize = 13.sp)
            Text("Aadhithya Somu", color = TextPrim, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(BgSurface, CircleShape)
                    .clickable { onNotifClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Notifications, contentDescription = null, tint = TextPrim, modifier = Modifier.size(20.dp))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(AccentRed, CircleShape)
                        .align(Alignment.TopEnd)
                        .offset(x = (-6).dp, y = 6.dp)
                )
            }
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(Brush.linearGradient(listOf(Accent, Color(0xFF7C3AED))), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("AS", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun CardPagerSection() {
    val pagerState = rememberPagerState { cards.size }
    var flippedCard by remember { mutableStateOf(-1) }
    var baseRotation by remember { mutableStateOf(0f) }
    Column {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 32.dp),
            pageSpacing = 16.dp,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            val card = cards[page]
            var isFlipped = flippedCard == page
            val rotation by animateFloatAsState(
                targetValue = baseRotation,
                animationSpec = tween(
                    durationMillis = 1200,
                    easing = FastOutSlowInEasing
                ),
                label = "flip"
            )
            FlippableCard(
                card = card,
                rotation = rotation,
                onFlip = {
                    val extraSpins = (2..3).random()
                    val target = if (isFlipped) 0f else 180f

                    baseRotation += (extraSpins * 360f) + target

                    isFlipped = !isFlipped
                }
            )
        }

        Spacer(Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(cards.size) { index ->
                val isSelected = pagerState.currentPage == index
                val width by animateDpAsState(if (isSelected) 20.dp else 6.dp, label = "dot")
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .height(6.dp)
                        .width(width)
                        .background(
                            if (isSelected) cards[pagerState.currentPage].accentColor else TextDim,
                            CircleShape
                        )
                )
            }
        }
    }
}

@Composable
fun FlippableCard(card: CardData, rotation: Float, onFlip: () -> Unit) {

    var tiltX by remember { mutableStateOf(0f) }
    var tiltY by remember { mutableStateOf(0f) }
    val normalized = rotation % 360f
    val animatedTiltX by animateFloatAsState(
        targetValue = tiltX,
        animationSpec = spring(stiffness = 200f)
    )
    val animatedTiltY by animateFloatAsState(
        targetValue = tiltY,
        animationSpec = spring(stiffness = 200f)
    )
    val scale by animateFloatAsState(
        targetValue = if (rotation in 1f..179f) 1.08f else 1f,
        animationSpec = tween(300)
    )
    val glowAlpha = ((rotation / 180f)).coerceIn(0f, 1f)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .shadow(
                elevation = 30.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = card.accentColor.copy(alpha = 0.4f + glowAlpha * 0.4f)
            )
            .graphicsLayer {
                rotationY = rotation + animatedTiltX * 10f
                rotationX = animatedTiltY * 10f
                scaleX = scale
                scaleY = scale
                cameraDistance = 16f * density
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { _, drag ->
                        tiltX = (tiltX + drag.x * 0.005f).coerceIn(-1f, 1f)
                        tiltY = (tiltY - drag.y * 0.005f).coerceIn(-1f, 1f)
                    },
                    onDragEnd = {
                        tiltX = 0f
                        tiltY = 0f
                    }
                )
            }
            .clickable { onFlip() }
    ) {
        val frontAlpha = if (normalized <= 90f || normalized >= 270f) 1f else 0f
        val backAlpha = if (normalized in 90f..270f) 1f else 0f

        Box(Modifier.alpha(frontAlpha)) {
            CardFront(card)
        }

        Box(
            Modifier
                .alpha(backAlpha)
                .graphicsLayer { rotationY = 180f }
        ) {
            CardBack(card)
        }

        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color.White.copy(alpha = 0.15f * glowAlpha),
                            Color.Transparent
                        ),
                        start = Offset.Zero,
                        end = Offset.Infinite
                    ),
                    RoundedCornerShape(24.dp)
                )
        )
    }
}
@Composable
fun CardFront(card: CardData) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    card.gradientColors,
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                ),
                RoundedCornerShape(24.dp)
            )
            .padding(22.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(Color.White.copy(alpha = 0.05f), radius = size.width * 0.6f,
                center = Offset(size.width * 1.1f, -size.height * 0.3f))
            drawCircle(Color.White.copy(alpha = 0.05f), radius = size.width * 0.4f,
                center = Offset(-size.width * 0.2f, size.height * 1.1f))
        }
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text("NEXUS BANK", color = Color.White, fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp, letterSpacing = 2.sp)
                Text(card.network, color = Color.White.copy(0.9f), fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    Modifier
                        .size(44.dp, 30.dp)
                        .background(
                            Brush.linearGradient(listOf(Color(0xFFFFD700), Color(0xFFFFA500))),
                            RoundedCornerShape(5.dp)
                        )
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawLine(Color.White.copy(0.4f), Offset(size.width / 2f, 0f),
                            Offset(size.width / 2f, size.height), strokeWidth = 1.dp.toPx())
                        drawLine(Color.White.copy(0.4f), Offset(0f, size.height / 2f),
                            Offset(size.width, size.height / 2f), strokeWidth = 1.dp.toPx())
                    }
                }
                Text(card.number, color = Color.White, fontSize = 18.sp, letterSpacing = 3.sp,
                    fontWeight = FontWeight.Medium)
            }
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.Bottom) {
                Column {
                    Text("CARD HOLDER", color = Color.White.copy(0.6f), fontSize = 9.sp, letterSpacing = 1.sp)
                    Text(card.holderName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("EXPIRES", color = Color.White.copy(0.6f), fontSize = 9.sp, letterSpacing = 1.sp)
                    Text(card.expiry, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun CardBack(card: CardData) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(listOf(Color(0xFF0D1525), Color(0xFF1A2535))),
                RoundedCornerShape(24.dp)
            )
    ) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Spacer(Modifier.height(30.dp))
            Box(Modifier.fillMaxWidth().height(48.dp).background(Color(0xFF111111)))
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 22.dp),
                Arrangement.SpaceBetween, Alignment.CenterVertically
            ) {
                Box(
                    Modifier.weight(1f).height(40.dp).background(Color.White),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text("  123  ", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
                }
                Spacer(Modifier.width(12.dp))
                Text("CVV", color = TextSub, fontSize = 12.sp)
            }
            Column(modifier = Modifier.padding(horizontal = 22.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Authorized Signature", color = TextSub, fontSize = 10.sp)
                Text("Helpline: 1800-123-456", color = TextPrim, fontSize = 12.sp)
                Text("nexusbank.in", color = card.accentColor, fontSize = 12.sp)
            }
        }
    }
}
@Composable
fun QuickStatsRow() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard("Total Balance", "₹4,03,500", "+2.4%", AccentGreen, Modifier.weight(1f))
        StatCard("This Month", "₹24,800", "-8.1%", AccentRed, Modifier.weight(1f))
    }
}

@Composable
fun StatCard(label: String, value: String, change: String, changeColor: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(BgSurface, RoundedCornerShape(18.dp))
            .border(1.dp, DividerLine, RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, color = TextSub, fontSize = 11.sp, letterSpacing = 0.5.sp)
            Text(value, color = TextPrim, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(
                    if (changeColor == AccentGreen) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null, tint = changeColor, modifier = Modifier.size(14.dp)
                )
                Text(change, color = changeColor, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Text("vs last month", color = TextDim, fontSize = 10.sp)
            }
        }
    }
}
@Composable
fun ActionGrid(onSend: () -> Unit) {
    val actions = listOf(
        Triple("Send", Icons.Default.Send, Accent),
        Triple("Pay", Icons.Default.Star, Color(0xFF10B981)),
        Triple("Scan", Icons.Default.Search, Color(0xFFF59E0B)),
        Triple("Top Up", Icons.Default.Add, Color(0xFFEC4899)),
        Triple("History", Icons.Default.Refresh, Color(0xFF8B5CF6)),
        Triple("More", Icons.Default.Menu, TextSub),
    )

    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        SectionHeader("Quick Actions")
        Spacer(Modifier.height(14.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            actions.forEach { (label, icon, color) ->
                ActionChip(
                    label = label,
                    icon = { Icon(icon, null, tint = color, modifier = Modifier.size(22.dp)) },
                    color = color,
                    onClick = if (label == "Send") onSend else ({})
                )
            }
        }
    }
}

@Composable
fun ActionChip(label: String, icon: @Composable () -> Unit, color: Color, onClick: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (pressed) 0.92f else 1f, spring(dampingRatio = 0.5f), label = "scale")

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.graphicsLayer { scaleX = scale; scaleY = scale }
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .background(color.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                .border(1.dp, color.copy(0.25f), RoundedCornerShape(16.dp))
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = { pressed = true; tryAwaitRelease(); pressed = false },
                        onTap = { onClick() }
                    )
                },
            contentAlignment = Alignment.Center
        ) { icon() }
        Text(label, color = TextSub, fontSize = 11.sp)
    }
}
@Composable
fun SpendingChart() {
    var animProgress by remember { mutableStateOf(0f) }
    var selectedSlice by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        animate(0f, 1f, animationSpec = tween(1200, easing = FastOutSlowInEasing)) { v, _ ->
            animProgress = v
        }
    }

    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        SectionHeader("Spending Breakdown")
        Spacer(Modifier.height(14.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BgSurface, RoundedCornerShape(20.dp))
                .border(1.dp, DividerLine, RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(130.dp), contentAlignment = Alignment.Center) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawDonutChart(spendingData, animProgress, selectedSlice)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(spendingData[selectedSlice].amount, color = TextPrim,
                                fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(spendingData[selectedSlice].label, color = TextSub, fontSize = 10.sp)
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                        spendingData.forEachIndexed { i, cat ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.clickable { selectedSlice = i }
                            ) {
                                Box(Modifier.size(8.dp).background(cat.color, CircleShape))
                                Text(cat.label,
                                    color = if (selectedSlice == i) TextPrim else TextSub,
                                    fontSize = 12.sp, modifier = Modifier.weight(1f))
                                Text("${(cat.percent * 100).toInt()}%",
                                    color = cat.color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    spendingData.forEach { cat ->
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                                Text(cat.label, color = TextSub, fontSize = 10.sp)
                                Text(cat.amount, color = TextPrim, fontSize = 10.sp)
                            }
                            Box(Modifier.fillMaxWidth().height(5.dp).background(DividerLine, CircleShape)) {
                                Box(
                                    Modifier
                                        .fillMaxWidth(cat.percent * animProgress)
                                        .height(5.dp)
                                        .background(
                                            Brush.horizontalGradient(listOf(cat.color, cat.color.copy(0.5f))),
                                            CircleShape
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun DrawScope.drawDonutChart(data: List<SpendingCategory>, progress: Float, selected: Int) {
    val strokeWidth = 22.dp.toPx()
    val radius = (size.minDimension / 2f) - strokeWidth / 2f
    val center = Offset(size.width / 2f, size.height / 2f)
    var startAngle = -90f
    data.forEachIndexed { i, cat ->
        val sweep = cat.percent * 360f * progress
        val isSelected = i == selected
        drawArc(
            color = cat.color.copy(if (isSelected) 1f else 0.5f),
            startAngle = startAngle + 1f,
            sweepAngle = sweep - 2f,
            useCenter = false,
            style = Stroke(if (isSelected) strokeWidth + 4.dp.toPx() else strokeWidth, cap = StrokeCap.Round),
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2)
        )
        startAngle += sweep
    }
}

@Composable
fun TransactionSection() {
    var filter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Income", "Shopping", "Food", "Entertainment", "Travel")

    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            SectionHeader("Transactions")
            Text("See All", color = Accent, fontSize = 13.sp, modifier = Modifier.clickable { })
        }
        Spacer(Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filters) { f -> FilterChip(f, filter == f) { filter = f } }
        }
        Spacer(Modifier.height(14.dp))
        val filtered = if (filter == "All") transactions else transactions.filter { it.category == filter }
        filtered.forEachIndexed { i, tx ->
            TransactionRow(tx, i)
            if (i < filtered.lastIndex) {
                Divider(color = DividerLine, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 4.dp))
            }
        }
    }
}
@Composable
fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val bg by animateColorAsState(if (selected) Accent.copy(0.15f) else BgSurface, label = "chipBg")
    val border by animateColorAsState(if (selected) Accent else DividerLine, label = "chipBorder")
    val textColor by animateColorAsState(if (selected) Accent else TextSub, label = "chipText")

    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(20.dp))
            .border(1.dp, border, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(label, color = textColor, fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
    }
}
@Composable
fun TransactionRow(tx: Transaction, index: Int) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(index * 60L); visible = true }

    AnimatedVisibility(visible = visible, enter = fadeIn() + slideInHorizontally { 40 }) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { }
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            if (tx.isCredit) AccentGreen.copy(0.12f) else BgSurface,
                            RoundedCornerShape(14.dp)
                        )
                        .border(
                            1.dp,
                            if (tx.isCredit) AccentGreen.copy(0.3f) else DividerLine,
                            RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(tx.icon, fontSize = 20.sp)
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(tx.title, color = TextPrim, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text(tx.subtitle, color = TextSub, fontSize = 11.sp)
                }
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    tx.amount,
                    color = if (tx.isCredit) AccentGreen else TextPrim,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(tx.date, color = TextDim, fontSize = 10.sp)
            }
        }
    }
}
@Composable
fun BottomNavBar(selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    val navItems = listOf(
        Icons.Default.Home to "Home",
        Icons.Default.Info to "Analytics",
        Icons.Default.AccountBox to "Cards",
        Icons.Default.Person to "Profile"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .background(BgSurface.copy(0.97f), RoundedCornerShape(28.dp))
            .border(1.dp, DividerLine, RoundedCornerShape(28.dp))
            .padding(vertical = 10.dp, horizontal = 8.dp)
    ) {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceAround, Alignment.CenterVertically) {
            navItems.forEachIndexed { i, (icon, label) ->
                val isSelected = selected == i
                val scale by animateFloatAsState(if (isSelected) 1f else 0.9f, label = "navScale")
                val color by animateColorAsState(if (isSelected) Accent else TextDim, label = "navColor")

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .clickable { onSelect(i) }
                        .graphicsLayer { scaleX = scale; scaleY = scale }
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(Accent.copy(0.15f), RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
                        }
                    } else {
                        Icon(icon, null, tint = color,
                            modifier = Modifier.size(20.dp).padding(vertical = 4.dp))
                    }
                    Text(label, color = color, fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal)
                }
            }
        }
    }
}

// ─── Notification Panel ───────────────────────────────────────────────────────

@Composable
fun NotificationPanel(onDismiss: () -> Unit) {
    val notifs = listOf(
        "💳 Card ending 3456 used at Amazon for ₹2,500",
        "✅ Salary of ₹50,000 credited",
        "⚠️ Unusual login attempt detected",
    )
    Card(
        modifier = Modifier.width(300.dp).padding(top = 80.dp, end = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BgSurface),
        border = BorderStroke(1.dp, DividerLine),
        elevation = CardDefaults.cardElevation(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text("Notifications", color = TextPrim, fontWeight = FontWeight.Bold)
                Icon(Icons.Default.Close, null, tint = TextSub,
                    modifier = Modifier.size(18.dp).clickable { onDismiss() })
            }
            notifs.forEach { notif ->
                Text(notif, color = TextSub, fontSize = 12.sp, modifier = Modifier.padding(vertical = 2.dp))
                Divider(color = DividerLine)
            }
        }
    }
}

// ─── Send Money Sheet ─────────────────────────────────────────────────────────

@Composable
fun SendMoneySheet(onDismiss: () -> Unit) {
    val contacts = listOf(
        "👤 Rahul" to "UPI: rahul@upi",
        "👤 Priya" to "UPI: priya@upi",
        "👤 Kiran" to "Bank Transfer"
    )
    var amount by remember { mutableStateOf("") }
    var selectedContact by remember { mutableStateOf(-1) }
    val scope = rememberCoroutineScope()
    var showSuccess by remember { mutableStateOf(false) }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black.copy(0.6f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(BgCard, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .padding(24.dp)
                .clickable(enabled = false) { }
        ) {
            Box(
                Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .background(DividerLine, CircleShape)
                    .align(Alignment.CenterHorizontally)
            )
            Spacer(Modifier.height(16.dp))
            Text("Send Money", color = TextPrim, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BgSurface, RoundedCornerShape(14.dp))
                    .border(1.dp, if (amount.isEmpty()) DividerLine else Accent, RoundedCornerShape(14.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("₹", color = if (amount.isEmpty()) TextDim else Accent,
                        fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(8.dp))
                    androidx.compose.foundation.text.BasicTextField(
                        value = amount,
                        onValueChange = { if (it.all { c -> c.isDigit() }) amount = it },
                        textStyle = androidx.compose.ui.text.TextStyle(
                            color = TextPrim, fontSize = 22.sp, fontWeight = FontWeight.Bold
                        ),
                        singleLine = true,
                        decorationBox = { inner ->
                            if (amount.isEmpty()) Text("0", color = TextDim,
                                fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            inner()
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Text("Recent Contacts", color = TextSub, fontSize = 12.sp)
            Spacer(Modifier.height(10.dp))
            contacts.forEachIndexed { i, (name, desc) ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .background(
                            if (selectedContact == i) Accent.copy(0.1f) else BgSurface,
                            RoundedCornerShape(12.dp)
                        )
                        .border(
                            1.dp,
                            if (selectedContact == i) Accent.copy(0.4f) else DividerLine,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { selectedContact = i }
                        .padding(12.dp),
                    Arrangement.SpaceBetween, Alignment.CenterVertically
                ) {
                    Column {
                        Text(name, color = TextPrim, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text(desc, color = TextSub, fontSize = 11.sp)
                    }
                    if (selectedContact == i)
                        Icon(Icons.Default.CheckCircle, null, tint = Accent, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.height(8.dp))
            }
            Spacer(Modifier.height(8.dp))
            AnimatedContent(showSuccess, label = "sendBtn") { success ->
                Button(
                    onClick = {
                        if (amount.isNotEmpty() && selectedContact >= 0) {
                            scope.launch { showSuccess = true; delay(1500); onDismiss() }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (success) AccentGreen else Accent
                    )
                ) {
                    if (success) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, null, tint = Color.White)
                            Text("Sent Successfully!", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Send, null, tint = Color.White)
                            Text("Send Money", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun SpendingChartDialog(onDismiss: () -> Unit) {
    var selectedChart by remember { mutableStateOf("Donut") }
    var selectedRange by remember { mutableStateOf("Monthly") }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(0.6f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .background(BgSurface, RoundedCornerShape(24.dp))
                .border(1.dp, DividerLine, RoundedCornerShape(24.dp))
                .padding(20.dp)
                .clickable(enabled = false) {}
        ) {
            Column {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Spending Breakdown",
                        color = TextPrim,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        Icons.Default.Close,
                        contentDescription = null,
                        tint = TextSub,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { onDismiss() }
                    )
                }
                Spacer(Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf("Donut", "Bar", "Line").forEach { type ->
                        FilterChip(
                            label = type,
                            selected = selectedChart == type
                        ) {
                            selectedChart = type
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))

                when (selectedChart) {
                    "Donut" -> DonutChartView(selectedRange)
                    "Bar" -> BarChartView(selectedRange)
                    "Line" -> LineChartView(selectedRange)
                }
                Spacer(Modifier.height(10.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf("Weekly", "Monthly", "Yearly").forEach { range ->
                        FilterChip(
                            label = range,
                            selected = selectedRange == range
                        ) {
                            selectedRange = range
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun DonutChartView(range: String) {
    SpendingChart()
}
@Composable
fun BarChartView(range: String) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        spendingData.forEach { cat ->
            Column {
                Text(cat.label, color = TextSub, fontSize = 12.sp)

                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(18.dp)
                        .background(DividerLine, RoundedCornerShape(6.dp))
                ) {
                    Box(
                        Modifier
                            .fillMaxWidth(cat.percent)
                            .height(18.dp)
                            .background(cat.color, RoundedCornerShape(6.dp))
                    )
                }
            }
        }
    }
}
@Composable
fun LineChartView(range: String) {

    val points = listOf(50f, 80f, 30f, 90f, 60f, 110f)
    val labels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

    Column {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .padding(horizontal = 8.dp)
        ) {

            val maxY = points.maxOrNull() ?: 1f
            val stepX = size.width / (points.size - 1)
            val gridLines = 4
            repeat(gridLines + 1) { i ->
                val y = size.height * i / gridLines
                drawLine(
                    color = DividerLine,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            val path = Path()
            val fillPath = Path()

            points.forEachIndexed { i, value ->
                val x = i * stepX
                val y = size.height - (value / maxY * size.height)

                if (i == 0) {
                    path.moveTo(x, y)
                    fillPath.moveTo(x, size.height)
                    fillPath.lineTo(x, y)
                } else {
                    path.lineTo(x, y)
                    fillPath.lineTo(x, y)
                }

                // 🔵 POINT DOT
                drawCircle(
                    color = Accent,
                    radius = 5.dp.toPx(),
                    center = Offset(x, y)
                )

                // 💰 VALUE LABEL
                drawContext.canvas.nativeCanvas.drawText(
                    value.toInt().toString(),
                    x,
                    y - 12.dp.toPx(),
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        textAlign = android.graphics.Paint.Align.CENTER
                        textSize = 28f
                        isFakeBoldText = true
                    }
                )
            }

            // Close fill path
            fillPath.lineTo(size.width, size.height)
            fillPath.close()

            // 🌈 GRADIENT AREA
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Accent.copy(alpha = 0.4f),
                        Color.Transparent
                    )
                )
            )

            // 📈 MAIN LINE
            drawPath(
                path = path,
                color = Accent,
                style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        // 📅 X-AXIS LABELS
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            labels.forEach {
                Text(
                    it,
                    color = TextSub,
                    fontSize = 11.sp
                )
            }
        }
    }
}
// ─── Helpers ──────────────────────────────────────────────────────────────────

@Composable
fun SectionHeader(text: String) {
    Text(text, color = TextPrim, fontSize = 16.sp, fontWeight = FontWeight.Bold)
}