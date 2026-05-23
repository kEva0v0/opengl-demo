package com.mashiro.chat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mashiro.chat.ui.mvi.ChatEffect
import com.mashiro.chat.ui.mvi.ChatIntent
import com.mashiro.chat.ui.mvi.ChatUiState
import com.mashiro.dataengine.chat.model.ChatMessage
import com.mashiro.dataengine.chat.model.ChatSender

@Composable
fun ChatRoute(viewModel: ChatViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val effect by viewModel.effects.collectAsState()

    ChatScreen(
        state = uiState,
        effect = effect,
        onIntent = viewModel::onIntent
    )
}

@Composable
fun ChatScreen(
    state: ChatUiState,
    effect: ChatEffect,
    onIntent: (ChatIntent) -> Unit
) {
    val listState = rememberLazyListState()

    LaunchedEffect(effect, state.messages.size) {
        if (effect == ChatEffect.ScrollToLatest) {
            if (state.messages.isNotEmpty()) {
                listState.animateScrollToItem(state.messages.lastIndex)
            }
            onIntent(ChatIntent.EffectConsumed)
        }
    }

    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF4F4F4))
        ) {
            ChatTitle(chatName = state.chatName)
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                state = listState
            ) {
                items(state.messages, key = { it.id }) { message ->
                    ChatBubble(message = message)
                }
            }
            ChatInputBar(
                inputText = state.inputText,
                onInputChange = { text ->
                    onIntent(ChatIntent.InputChanged(text))
                },
                onSendMessage = {
                    onIntent(ChatIntent.SendClicked)
                }
            )
        }
    }
}

@Composable
private fun ChatTitle(chatName: String) {
    Surface(
        color = Color.White,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = chatName,
                color = Color(0xFF222222),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    val isMine = message.sender == ChatSender.ME
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            color = if (isMine) Color(0xFF95EC69) else Color.White,
            shape = RoundedCornerShape(6.dp),
            elevation = 0.dp,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = message.content,
                color = Color(0xFF222222),
                fontSize = 15.sp,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun ChatInputBar(
    inputText: String,
    onInputChange: (String) -> Unit,
    onSendMessage: () -> Unit
) {
    Surface(
        color = Color.White,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = onInputChange,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                singleLine = true,
                placeholder = {
                    Text(text = "输入消息")
                }
            )
            Button(
                onClick = onSendMessage,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .height(48.dp)
            ) {
                Text(text = "发送")
            }
        }
    }
}
