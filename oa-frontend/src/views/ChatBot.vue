<template>
  <div class="chat-container">
    <!-- 头部 -->
    <div class="chat-header">
      <div class="header-content">
        <div class="avatar">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"/>
          </svg>
        </div>
        <div class="info">
          <h3>知识百科</h3>
          <span class="status online">在线</span>
        </div>
      </div>
    </div>

    <!-- 消息列表 -->
    <div ref="messageList" class="message-list">
      <div class="welcome-message">
        <div class="bot-avatar">🤖</div>
        <div class="message-content">
          <p>您好！我是知识百科小助手，很高兴为您服务！</p>
          <p>请问有什么可以帮助您的吗？</p>
        </div>
      </div>
      
      <div v-for="msg in messages" :key="msg.id" :class="['message-item', msg.type]">
        <div v-if="msg.type === 'user'" class="user-avatar">👤</div>
        <div v-else class="bot-avatar">🤖</div>
        <div class="message-bubble">
          <p>{{ msg.content }}</p>
          <span class="time">{{ msg.time }}</span>
        </div>
      </div>
      
      <div v-if="isTyping" class="typing-indicator">
        <div class="bot-avatar">🤖</div>
        <div class="message-bubble typing">
          <span class="dot"></span>
          <span class="dot"></span>
          <span class="dot"></span>
        </div>
      </div>
    </div>

    <!-- 输入区域 -->
    <div class="input-area">
      <div class="quick-questions">
        <button v-for="q in quickQuestions" :key="q" @click="sendQuickQuestion(q)">
          {{ q }}
        </button>
      </div>
      <div class="input-row">
        <input 
          v-model="inputMessage" 
          @keyup.enter="sendMessage" 
          placeholder="输入您的问题..." 
          class="message-input"
          :disabled="isTyping"
        />
        <button @click="sendMessage" :disabled="!inputMessage.trim() || isTyping" class="send-btn">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M22 2L11 13"/>
            <path d="M22 2l-7 20-4-9-9-4 20-7z"/>
          </svg>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from 'vue'
import { chatApi } from '../api/chat'

const messages = ref([])
const inputMessage = ref('')
const isTyping = ref(false)
const messageList = ref(null)

const quickQuestions = [
  '如何下单？',
  '配送时间是多久？',
  '退货政策是什么？',
  '有优惠活动吗？'
]

const formatTime = () => {
  const now = new Date()
  return `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}`
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messageList.value) {
      messageList.value.scrollTop = messageList.value.scrollHeight
    }
  })
}

const addMessage = (content, type) => {
  messages.value.push({
    id: Date.now(),
    content,
    type,
    time: formatTime()
  })
  scrollToBottom()
}

const sendMessage = async () => {
  if (!inputMessage.value.trim() || isTyping.value) return
  
  const userMessage = inputMessage.value.trim()
  addMessage(userMessage, 'user')
  inputMessage.value = ''
  isTyping.value = true
  
  try {
    const response = await chatApi.sendMessage(userMessage)
    addMessage(response.data.reply, 'bot')
  } catch (error) {
    addMessage('抱歉，我现在有点忙，请稍后再试。', 'bot')
  } finally {
    isTyping.value = false
  }
}

const sendQuickQuestion = (question) => {
  inputMessage.value = question
  sendMessage()
}

onMounted(() => {
  scrollToBottom()
})
</script>

<style scoped>
.chat-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.chat-header {
  padding: 16px 20px;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
}

.header-content {
  display: flex;
  align-items: center;
  gap: 12px;
}

.avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 20px;
}

.info h3 {
  margin: 0;
  color: white;
  font-size: 18px;
}

.status {
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.2);
  color: white;
}

.status.online {
  background: #10b981;
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.message-list::-webkit-scrollbar {
  width: 6px;
}

.message-list::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 3px;
}

.message-list::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.3);
  border-radius: 3px;
}

.welcome-message {
  display: flex;
  gap: 12px;
  align-self: flex-start;
}

.message-item {
  display: flex;
  gap: 12px;
}

.message-item.user {
  flex-direction: row-reverse;
}

.user-avatar,
.bot-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
}

.user-avatar {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
}

.bot-avatar {
  background: rgba(255, 255, 255, 0.2);
}

.message-bubble {
  max-width: 70%;
  padding: 12px 16px;
  border-radius: 18px;
  background: white;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.message-item.user .message-bubble {
  background: #10b981;
  color: white;
  border-radius: 18px 18px 4px 18px;
}

.message-item.bot .message-bubble {
  border-radius: 18px 18px 18px 4px;
}

.message-bubble p {
  margin: 0 0 8px 0;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-word;
}

.message-bubble .time {
  font-size: 11px;
  opacity: 0.6;
}

.message-item.user .time {
  color: rgba(255, 255, 255, 0.7);
}

.typing-indicator {
  display: flex;
  gap: 12px;
  align-self: flex-start;
}

.typing-indicator .message-bubble {
  padding: 12px 20px;
  display: flex;
  gap: 4px;
}

.dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #9ca3af;
  animation: typing 1.4s infinite ease-in-out;
}

.dot:nth-child(2) {
  animation-delay: 0.2s;
}

.dot:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%, 80%, 100% {
    opacity: 0.2;
    transform: scale(0.8);
  }
  40% {
    opacity: 1;
    transform: scale(1);
  }
}

.input-area {
  padding: 16px 20px;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
}

.quick-questions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 12px;
}

.quick-questions button {
  padding: 6px 14px;
  background: rgba(255, 255, 255, 0.2);
  border: none;
  border-radius: 20px;
  color: white;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.3s;
}

.quick-questions button:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: translateY(-2px);
}

.input-row {
  display: flex;
  gap: 12px;
  align-items: center;
}

.message-input {
  flex: 1;
  padding: 12px 16px;
  border: none;
  border-radius: 24px;
  font-size: 14px;
  outline: none;
  background: white;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.message-input::placeholder {
  color: #9ca3af;
}

.message-input:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.send-btn {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  border: none;
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
  box-shadow: 0 4px 15px rgba(16, 185, 129, 0.4);
}

.send-btn:hover:not(:disabled) {
  transform: scale(1.05);
  box-shadow: 0 6px 20px rgba(16, 185, 129, 0.5);
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>