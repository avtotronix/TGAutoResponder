package com.tgautoresponder.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tgautoresponder.app.data.ContactConfig
import com.tgautoresponder.app.data.PreferencesManager
import com.tgautoresponder.app.service.TelegramNotificationService
import com.tgautoresponder.app.ui.theme.*

class MainActivity : ComponentActivity() {

    private lateinit var prefs: PreferencesManager
    private val contactsUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // Handled via state trigger
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = PreferencesManager(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(
                contactsUpdateReceiver,
                IntentFilter(TelegramNotificationService.ACTION_CONTACTS_UPDATED),
                RECEIVER_NOT_EXPORTED
            )
        } else {
            registerReceiver(
                contactsUpdateReceiver,
                IntentFilter(TelegramNotificationService.ACTION_CONTACTS_UPDATED)
            )
        }

        setContent {
            TGAutoResponderTheme {
                MainScreen(prefs = prefs)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(contactsUpdateReceiver)
        } catch (_: Exception) {}
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(prefs: PreferencesManager) {
    val context = LocalContext.current

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Главная, 1: Чаты, 2: История
    var isEnabled by remember { mutableStateOf(prefs.isEnabled) }
    var replyText by remember { mutableStateOf(prefs.replyText) }
    var cooldownMinutes by remember { mutableIntStateOf(prefs.cooldownMinutes) }
    var hasPermission by remember { mutableStateOf(checkNotificationAccess(context)) }
    var contacts by remember { mutableStateOf(prefs.getContacts()) }
    var logs by remember { mutableStateOf(prefs.getLogs()) }

    var editingContact by remember { mutableStateOf<ContactConfig?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    val templates = listOf(
        "Привет! Я сейчас занят, отвечу позже 👋",
        "За рулем 🚗 Напишу как освобожусь!",
        "На совещании 🤫 Отвечу позже.",
        "Уже сплю 😴 Завтра свяжемся!"
    )

    val cooldownOptions = listOf(1, 5, 10, 30, 60)

    Scaffold(
        containerColor = BgBlack,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(if (isEnabled && hasPermission) NeonPink else Color.Gray)
                                .border(2.dp, if (isEnabled && hasPermission) HotPink else Color.Transparent, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "TG AutoResponder",
                                fontWeight = FontWeight.Bold,
                                color = TextWhite,
                                fontSize = 19.sp
                            )
                            Text(
                                text = "v1.0.1 Pink Edition",
                                color = NeonPink,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceBlack)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceBlack,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.FlashOn, contentDescription = null) },
                    label = { Text("Главная", fontSize = 12.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonPink,
                        selectedTextColor = NeonPink,
                        indicatorColor = BorderPink,
                        unselectedIconColor = TextDarkMuted,
                        unselectedTextColor = TextDarkMuted
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = {
                        contacts = prefs.getContacts()
                        selectedTab = 1
                    },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (contacts.isNotEmpty()) {
                                    Badge(containerColor = NeonPink) {
                                        Text("${contacts.size}", color = TextWhite)
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.People, contentDescription = null)
                        }
                    },
                    label = { Text("Чаты и люди", fontSize = 12.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonPink,
                        selectedTextColor = NeonPink,
                        indicatorColor = BorderPink,
                        unselectedIconColor = TextDarkMuted,
                        unselectedTextColor = TextDarkMuted
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = {
                        logs = prefs.getLogs()
                        selectedTab = 2
                    },
                    icon = { Icon(Icons.Default.History, contentDescription = null) },
                    label = { Text("История", fontSize = 12.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonPink,
                        selectedTextColor = NeonPink,
                        indicatorColor = BorderPink,
                        unselectedIconColor = TextDarkMuted,
                        unselectedTextColor = TextDarkMuted
                    )
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                0 -> {
                    // TAB 1: ГЛАВНАЯ
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        if (!hasPermission) {
                            item {
                                PermissionWarningCard(onGrantClick = {
                                    context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                                })
                            }
                        }

                        // Master status switch
                        item {
                            StatusToggleCard(
                                isEnabled = isEnabled,
                                onToggle = { newValue ->
                                    isEnabled = newValue
                                    prefs.isEnabled = newValue
                                }
                            )
                        }

                        // Global reply text editor
                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = CardBlack),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, BorderPink, RoundedCornerShape(16.dp))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.ChatBubble,
                                            contentDescription = null,
                                            tint = NeonPink,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Общий текст автоответа",
                                            fontWeight = FontWeight.Bold,
                                            color = TextWhite,
                                            fontSize = 16.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Отправляется всем людям, для которых не настроено индивидуальное сообщение.",
                                        fontSize = 12.sp,
                                        color = TextPinkMuted
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    OutlinedTextField(
                                        value = replyText,
                                        onValueChange = {
                                            replyText = it
                                            prefs.replyText = it
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        placeholder = { Text("Введите текст ответа...", color = TextDarkMuted) },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = NeonPink,
                                            unfocusedBorderColor = BorderPink,
                                            focusedTextColor = TextWhite,
                                            unfocusedTextColor = TextWhite
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        minLines = 3,
                                        maxLines = 5
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Text(
                                        text = "Быстрые шаблоны:",
                                        fontSize = 12.sp,
                                        color = TextPinkMuted,
                                        fontWeight = FontWeight.Medium
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        templates.forEach { template ->
                                            Surface(
                                                color = SurfaceBlack,
                                                shape = RoundedCornerShape(8.dp),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, BorderPink),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        replyText = template
                                                        prefs.replyText = template
                                                    }
                                            ) {
                                                Text(
                                                    text = template,
                                                    color = TextWhite,
                                                    fontSize = 13.sp,
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Cooldown card
                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = CardBlack),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, BorderPink, RoundedCornerShape(16.dp))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Timer,
                                            contentDescription = null,
                                            tint = NeonPink,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Интервал повтора (Кулдаун)",
                                            fontWeight = FontWeight.Bold,
                                            color = TextWhite,
                                            fontSize = 16.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Не спамить: отвечать одному человеку не чаще раза в:",
                                        fontSize = 12.sp,
                                        color = TextPinkMuted
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        cooldownOptions.forEach { minutes ->
                                            val selected = cooldownMinutes == minutes
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(if (selected) NeonPink else SurfaceBlack)
                                                    .border(1.dp, if (selected) HotPink else BorderPink, RoundedCornerShape(10.dp))
                                                    .clickable {
                                                        cooldownMinutes = minutes
                                                        prefs.cooldownMinutes = minutes
                                                    }
                                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "${minutes}м",
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (selected) Color.White else TextPinkMuted,
                                                    fontSize = 14.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // TAB 2: ЧАТЫ И ЛЮДИ (Персональная настройка)
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = CardBlack),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, BorderPink, RoundedCornerShape(16.dp))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Личные чаты людей",
                                                fontWeight = FontWeight.Bold,
                                                color = TextWhite,
                                                fontSize = 17.sp
                                            )
                                            Text(
                                                text = "Каналы и группы автоматически исключены.",
                                                color = NeonPink,
                                                fontSize = 12.sp
                                            )
                                        }

                                        Button(
                                            onClick = { showAddDialog = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = NeonPink),
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = null, tint = TextWhite, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("+ Контакт", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Чаты появляются автоматически, когда люди пишут вам. Вы можете настроить особое сообщение для любого контакта или полностью выключить автоответ для него.",
                                        fontSize = 12.sp,
                                        color = TextPinkMuted,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }

                        if (contacts.isEmpty()) {
                            item {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = SurfaceBlack),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 20.dp)
                                        .border(1.dp, BorderPink, RoundedCornerShape(16.dp))
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PersonSearch,
                                            contentDescription = null,
                                            tint = NeonPink,
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = "Пока нет обнаруженных чатов",
                                            fontWeight = FontWeight.Bold,
                                            color = TextWhite,
                                            fontSize = 16.sp
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Как только вам напишет первый человек в Telegram, он появится здесь. Либо добавьте контакт вручную кнопкой «+ Контакт».",
                                            color = TextPinkMuted,
                                            fontSize = 13.sp,
                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                        )
                                    }
                                }
                            }
                        } else {
                            items(contacts, key = { it.name }) { contact ->
                                ContactItemCard(
                                    contact = contact,
                                    onToggle = { enabled ->
                                        prefs.toggleContactEnabled(contact.name, enabled)
                                        contacts = prefs.getContacts()
                                    },
                                    onEdit = {
                                        editingContact = contact
                                    },
                                    onDelete = {
                                        prefs.deleteContact(contact.name)
                                        contacts = prefs.getContacts()
                                    }
                                )
                            }
                        }
                    }
                }

                2 -> {
                    // TAB 3: ИСТОРИЯ
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "История автоответов",
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite,
                                    fontSize = 17.sp
                                )
                                IconButton(onClick = { logs = prefs.getLogs() }) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Обновить",
                                        tint = NeonPink
                                    )
                                }
                            }
                        }

                        if (logs.isEmpty()) {
                            item {
                                Text(
                                    text = "Ответов пока не было отправлено",
                                    color = TextPinkMuted,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(vertical = 20.dp)
                                )
                            }
                        } else {
                            items(logs) { log ->
                                Surface(
                                    color = CardBlack,
                                    shape = RoundedCornerShape(10.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderPink),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = log,
                                        color = TextWhite,
                                        fontSize = 13.sp,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog for editing contact settings
    editingContact?.let { contact ->
        EditContactDialog(
            contact = contact,
            onDismiss = { editingContact = null },
            onSave = { updated ->
                prefs.saveContact(updated)
                contacts = prefs.getContacts()
                editingContact = null
            }
        )
    }

    // Dialog for adding contact manually
    if (showAddDialog) {
        AddContactDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { newContact ->
                prefs.saveContact(newContact)
                contacts = prefs.getContacts()
                showAddDialog = false
            }
        )
    }
}

@Composable
fun ContactItemCard(
    contact: ContactConfig,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (contact.isEnabled) CardBlack else SurfaceBlack
        ),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (contact.isEnabled && contact.customMessage.isNotBlank()) NeonPink else BorderPink,
                RoundedCornerShape(14.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar with initial letter
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (contact.isEnabled) NeonPink else Color(0xFF3A2030)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = contact.name.take(1).uppercase(),
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contact.name,
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                if (!contact.isEnabled) {
                    Text(
                        text = "⛔ Автоответ отключен",
                        color = StatusRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                } else if (contact.customMessage.isNotBlank()) {
                    Text(
                        text = "💬 «${contact.customMessage}»",
                        color = HotPink,
                        fontSize = 12.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    Text(
                        text = "⚙️ Стандартный ответ",
                        color = TextPinkMuted,
                        fontSize = 12.sp
                    )
                }
            }

            // Quick Toggle
            Switch(
                checked = contact.isEnabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = TextWhite,
                    checkedTrackColor = NeonPink,
                    uncheckedThumbColor = TextDarkMuted,
                    uncheckedTrackColor = SurfaceBlack
                )
            )

            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Редактировать", tint = NeonPink, modifier = Modifier.size(20.dp))
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "Удалить", tint = TextDarkMuted, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun EditContactDialog(
    contact: ContactConfig,
    onDismiss: () -> Unit,
    onSave: (ContactConfig) -> Unit
) {
    var isEnabled by remember { mutableStateOf(contact.isEnabled) }
    var customMessage by remember { mutableStateOf(contact.customMessage) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceBlack,
        title = {
            Column {
                Text(
                    text = "Настройка чата",
                    color = TextWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = contact.name,
                    color = NeonPink,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Включить автоответ", color = TextWhite, fontSize = 14.sp)
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { isEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = TextWhite,
                            checkedTrackColor = NeonPink,
                            uncheckedThumbColor = TextDarkMuted,
                            uncheckedTrackColor = CardBlack
                        )
                    )
                }

                Column {
                    Text(
                        text = "Персональный текст для этого человека:",
                        color = TextPinkMuted,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = customMessage,
                        onValueChange = { customMessage = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Оставьте пустым для общего ответа", color = TextDarkMuted, fontSize = 12.sp) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonPink,
                            unfocusedBorderColor = BorderPink,
                            focusedTextColor = TextWhite,
                            unfocusedTextColor = TextWhite
                        ),
                        shape = RoundedCornerShape(10.dp),
                        minLines = 3,
                        maxLines = 4
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(contact.copy(isEnabled = isEnabled, customMessage = customMessage.trim()))
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonPink)
            ) {
                Text("Сохранить", color = TextWhite, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена", color = TextPinkMuted)
            }
        }
    )
}

@Composable
fun AddContactDialog(
    onDismiss: () -> Unit,
    onAdd: (ContactConfig) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var customMessage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceBlack,
        title = {
            Text("Добавить контакт", color = TextWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Введите имя контакта ровно так, как он записан в Telegram:", color = TextPinkMuted, fontSize = 12.sp)
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Имя контакта (как в Telegram)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonPink,
                        unfocusedBorderColor = BorderPink,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedLabelColor = NeonPink,
                        unfocusedLabelColor = TextPinkMuted
                    ),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = customMessage,
                    onValueChange = { customMessage = it },
                    label = { Text("Особый ответ (необязательно)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonPink,
                        unfocusedBorderColor = BorderPink,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite,
                        focusedLabelColor = NeonPink,
                        unfocusedLabelColor = TextPinkMuted
                    ),
                    shape = RoundedCornerShape(10.dp),
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onAdd(ContactConfig(name = name.trim(), customMessage = customMessage.trim(), isEnabled = true))
                    }
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = NeonPink)
            ) {
                Text("Добавить", color = TextWhite, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена", color = TextPinkMuted)
            }
        }
    )
}

@Composable
fun StatusToggleCard(isEnabled: Boolean, onToggle: (Boolean) -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBlack),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, if (isEnabled) NeonPink else BorderPink, RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (isEnabled) "Автоответчик ВКЛЮЧЕН" else "Автоответчик ВЫКЛЮЧЕН",
                    fontWeight = FontWeight.Bold,
                    color = TextWhite,
                    fontSize = 17.sp
                )
                Text(
                    text = if (isEnabled) "🟢 Отвечает на входящие личные чаты" else "⚪ Ответы приостановлены",
                    color = if (isEnabled) NeonPink else TextDarkMuted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = TextWhite,
                    checkedTrackColor = NeonPink,
                    uncheckedThumbColor = TextDarkMuted,
                    uncheckedTrackColor = SurfaceBlack
                )
            )
        }
    }
}

@Composable
fun PermissionWarningCard(onGrantClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A1020)),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, NeonPink, RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = NeonPink
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Требуется доступ к уведомлениям",
                    fontWeight = FontWeight.Bold,
                    color = NeonPink,
                    fontSize = 16.sp
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Для работы автоответчика и обнаружения чатов включите доступ в системных настройках Android.",
                color = TextWhite.copy(alpha = 0.9f),
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onGrantClick,
                colors = ButtonDefaults.buttonColors(containerColor = NeonPink),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(text = "Разрешить доступ", color = TextWhite, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun checkNotificationAccess(context: Context): Boolean {
    val pkgName = context.packageName
    val flat = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners")
    return flat != null && flat.contains(pkgName)
}
