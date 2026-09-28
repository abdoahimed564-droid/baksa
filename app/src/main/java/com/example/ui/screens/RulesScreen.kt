package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.BekasaTopBar
import com.example.ui.theme.RoyalPurple
import com.example.ui.theme.TrophyGold
import com.example.ui.viewmodel.GameUiState
import com.example.ui.viewmodel.GameViewModel

@Composable
fun RulesScreen(
    uiState: GameUiState,
    viewModel: GameViewModel
) {
    BackHandler {
        viewModel.navigateBack()
    }

    val scrollState = rememberScrollState()

    val rules = listOf(
        Triple("1. السؤال المقفول 🔒", "في بداية كل جولة، يختار النظام سؤالاً عشوائياً محيراً من بنك 1000 سؤال، ويكون السؤال ثابتاً ومقفولاً لجميع اللاعبين ولا يمكن تغييره!", "❓"),
        Triple("2. مرحلة تأليف الهبدات في سرية 🤫", "يمرر الهاتف لكل لاعب بالترتيب ليقرأ السؤال ويكتب إجابته المفبركة بكل ثقة وذكاء، حتى لا يرى بقية اللاعبين ما كتبه!", "✍️"),
        Triple("3. مرحلة التصويت واختيار أفضل هبدة 🗳️", "بعد انتهاء الجميع، يمرر الهاتف لكل لاعب ليصوت لأكثر إجابة مقنعة، ولا يمكن لأي لاعب اختيار إجابته الخاصة أبداً!", "🎯"),
        Triple("4. إعلان الفائز وكشف أصحاب الهبدات 🏆", "الإجابة التي تحصل على أكبر عدد من الأصوات تفوز بالجولة، ويكشف النظام اسم صاحب الهبدة الأسطورية ويمنحه النقاط!", "👑"),
        Triple("5. كشف الحقيقة الصادمة 🤯", "في نهاية الجولة يتم كشف الإجابة الحقيقية الأصلية للسؤال ومقارنتها بهبدات اللاعبين وسط أجواء من الضحك!", "✨"),
        Triple("6. التتويج النهائي 👑", "اللاعب صاحب أكبر رصيد من النقاط وإقناع الأصدقاء يتوج بلقب ملك البكاسة والهبد في القعدة!", "🥇")
    )

    Scaffold(
        topBar = {
            BekasaTopBar(
                title = "كيف تلعب بكاسة؟ 📜",
                onBackClick = { viewModel.navigateBack() },
                soundEnabled = uiState.settings.soundEnabled,
                onSoundToggle = { viewModel.toggleSound() }
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Button(
                    onClick = { viewModel.navigateBack() },
                    colors = ButtonDefaults.buttonColors(containerColor = TrophyGold),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(54.dp)
                        .testTag("rules_got_it_button")
                ) {
                    Text(
                        text = "فهمنا القواعد.. يلا نبدأ الهبد! 🎭",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            rules.forEach { (title, desc, emoji) ->
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(text = emoji, fontSize = 28.sp)
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = RoyalPurple
                                )
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = desc,
                                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
