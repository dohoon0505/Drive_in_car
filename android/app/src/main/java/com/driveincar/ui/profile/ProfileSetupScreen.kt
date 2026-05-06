package com.driveincar.ui.profile

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.driveincar.ui.components.Overline
import com.driveincar.ui.components.PrimaryButton
import com.driveincar.ui.theme.ApexColors
import com.driveincar.ui.theme.Pretendard

@Composable
fun ProfileSetupScreen(
    onCompleted: () -> Unit,
    onBack: () -> Unit = {},
    vm: ProfileSetupViewModel = hiltViewModel(),
) {
    val state by vm.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(vm) {
        vm.events.collect { ev ->
            if (ev is ProfileSetupEvent.Saved) onCompleted()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ApexColors.Bg)
            .padding(horizontal = 24.dp),
    ) {
        Spacer(Modifier.height(48.dp))

        // 상단 chrome: 뒤로가기 + 진행 바 + STEP n/3
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = { if (state.step == 0) onBack() else vm.back() },
                modifier = Modifier.size(44.dp),
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로", tint = ApexColors.Text)
            }
            Spacer(Modifier.width(8.dp))
            ProgressBar(step = state.step, modifier = Modifier.weight(1f))
        }

        Spacer(Modifier.height(20.dp))
        Overline(
            text = "STEP ${state.step + 1} / 3",
            color = ApexColors.TextTer,
            tracking = 0.20f,
        )

        Spacer(Modifier.height(8.dp))

        AnimatedContent(
            targetState = state.step,
            label = "step",
            transitionSpec = {
                (fadeIn() togetherWith fadeOut()).using(SizeTransform(clip = false))
            },
            modifier = Modifier.weight(1f),
        ) { step ->
            when (step) {
                0 -> Step0Nickname(state = state, onNickname = vm::onNickname)
                1 -> Step1Brand(state = state, onBrand = vm::onBrand)
                else -> Step2Model(state = state, onModel = vm::onModel)
            }
        }

        if (state.error != null) {
            Text(
                state.error!!,
                color = ApexColors.Red,
                fontSize = 13.sp,
                fontFamily = Pretendard,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }

        if (state.isSubmitting) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = ApexColors.Brand)
            }
            Spacer(Modifier.height(8.dp))
        }

        PrimaryButton(
            label = if (state.step == 2) "시작하기" else "다음",
            onClick = vm::next,
            enabled = !state.isSubmitting,
        )
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ProgressBar(step: Int, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(3) { i ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .background(
                        if (i <= step) ApexColors.Brand else ApexColors.BgElevated,
                        RoundedCornerShape(2.dp),
                    ),
            )
        }
    }
}

@Composable
private fun StepHeader(title: String, subtitle: String) {
    Column {
        Spacer(Modifier.height(8.dp))
        Text(
            text = title,
            color = ApexColors.Text,
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = Pretendard,
            letterSpacing = (-0.030).em,
            lineHeight = 36.sp,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = subtitle,
            color = ApexColors.TextSec,
            fontSize = 15.sp,
            lineHeight = 22.sp,
            fontFamily = Pretendard,
        )
    }
}

@Composable
private fun Step0Nickname(state: ProfileSetupUiState, onNickname: (String) -> Unit) {
    Column {
        StepHeader(
            title = "어떻게 불러드릴까요?",
            subtitle = "랭킹과 코멘트에 표시될 닉네임이에요. 2~12자 한글/영문.",
        )
        Spacer(Modifier.height(28.dp))
        OutlinedTextField(
            value = state.nickname,
            onValueChange = onNickname,
            placeholder = { Text("닉네임", color = ApexColors.TextTer) },
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ApexColors.Brand,
                unfocusedBorderColor = ApexColors.BorderStrong,
                focusedContainerColor = ApexColors.BgRaised,
                unfocusedContainerColor = ApexColors.BgRaised,
                focusedTextColor = ApexColors.Text,
                unfocusedTextColor = ApexColors.Text,
                cursorColor = ApexColors.Brand,
            ),
            keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Text),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            listOf("ApexHunter", "NightDriver", "곡선마스터", "0to100").take(2).forEach { suggestion ->
                SuggestionChip(text = suggestion, onClick = { onNickname(suggestion) })
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            listOf("ApexHunter", "NightDriver", "곡선마스터", "0to100").drop(2).forEach { suggestion ->
                SuggestionChip(text = suggestion, onClick = { onNickname(suggestion) })
            }
        }
    }
}

@Composable
private fun SuggestionChip(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clickable { onClick() }
            .background(ApexColors.BgRaised, RoundedCornerShape(999.dp))
            .border(1.dp, ApexColors.Border, RoundedCornerShape(999.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Text(
            text = "+ $text",
            color = ApexColors.TextSec,
            fontSize = 13.sp,
            fontFamily = Pretendard,
        )
    }
}

@Composable
private fun Step1Brand(state: ProfileSetupUiState, onBrand: (String) -> Unit) {
    Column {
        StepHeader(
            title = buildAnnotatedString {
                append("어떤 메이커를\n")
                withStyle(SpanStyle(color = ApexColors.BrandLight)) { append("타고 계세요?") }
            }.toString(),
            subtitle = "같은 메이커끼리 따로 랭킹을 매겨요. 클래스별 비교가 정확해져요.",
        )
        Spacer(Modifier.height(20.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(CarBrands.all) { b ->
                BrandCard(
                    brand = b,
                    selected = state.carBrand == b.name,
                    onClick = { onBrand(b.name) },
                )
            }
        }
    }
}

@Composable
private fun BrandCard(brand: CarBrand, selected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(
                if (selected) ApexColors.BgElevated else ApexColors.BgRaised,
                RoundedCornerShape(14.dp),
            )
            .border(
                if (selected) 1.5.dp else 1.dp,
                if (selected) ApexColors.Brand else ApexColors.Border,
                RoundedCornerShape(14.dp),
            )
            .clickable { onClick() }
            .padding(14.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(brand.color, RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = brand.name.first().toString(),
                color = ApexColors.Text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Pretendard,
            )
        }
        Column {
            Text(
                text = brand.name,
                color = ApexColors.Text,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Pretendard,
            )
            Overline(text = brand.country, color = ApexColors.TextTer, tracking = 0.10f)
        }
    }
}

@Composable
private fun Step2Model(state: ProfileSetupUiState, onModel: (String) -> Unit) {
    val brand = CarBrands.byName(state.carBrand) ?: return
    Column {
        StepHeader(
            title = "정확히 어떤\n모델인가요?",
            subtitle = "${brand.name}의 라인업이에요. 정확한 차종이어야 클래스별 비교가 정확해요.",
        )
        Spacer(Modifier.height(20.dp))
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(brand.models) { model ->
                ModelRow(
                    brand = brand,
                    model = model,
                    selected = state.carModel == model,
                    onClick = { onModel(model) },
                )
            }
        }
    }
}

@Composable
private fun ModelRow(
    brand: CarBrand,
    model: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (selected) ApexColors.BgElevated else ApexColors.BgRaised,
                RoundedCornerShape(14.dp),
            )
            .border(
                if (selected) 1.5.dp else 1.dp,
                if (selected) ApexColors.Brand else ApexColors.Border,
                RoundedCornerShape(14.dp),
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // 라이센스 플레이트 스타일 슬러그
        Box(
            modifier = Modifier
                .width(44.dp)
                .height(28.dp)
                .background(brand.color, RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = brand.slug,
                color = ApexColors.Text,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Pretendard,
                letterSpacing = 0.05.em,
            )
        }
        Text(
            text = model,
            color = ApexColors.Text,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = Pretendard,
            modifier = Modifier.weight(1f),
        )
        if (selected) {
            Icon(
                Icons.Filled.Check,
                contentDescription = null,
                tint = ApexColors.Brand,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
