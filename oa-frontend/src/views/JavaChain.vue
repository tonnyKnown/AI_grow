<template>
  <div class="javachain-container">
    <!-- 页面头部 -->
    <div class="javachain-header">
      <h1>🚀 JavaChain</h1>
      <p class="subtitle">LangChain4j + DeepSeek + RAG + MCP 集成测试平台</p>
    </div>

    <!-- 主标签页导航 -->
    <div class="tabs">
      <button
        v-for="tab in MAIN_TABS"
        :key="tab.id"
        :class="['tab', { active: activeMainTab === tab.id }]"
        @click="activeMainTab = tab.id"
      >
        {{ tab.name }}
      </button>
    </div>

    <!-- 内容区域 -->
    <div class="tab-content" :class="{ active: true }">
      <!-- 简单对话 -->
      <div v-if="activeMainTab === 'chat'" class="panel">
        <div class="session-bar">
          <div class="session-info">
            <span class="session-label">当前会话:</span>
            <span class="session-id">{{ currentSessionId || '--' }}</span>
          </div>
          <div class="session-actions">
            <button class="btn btn-secondary btn-sm" @click="createNewSession()">📝 新建会话</button>
            <button class="btn btn-secondary btn-sm" @click="clearCurrentSession()">🗑️ 清空历史</button>
          </div>
        </div>
        <div class="chat-container">
          <div class="chat-messages" ref="chatMessagesRef">
            <div v-for="msg in chatMessagesList" :key="msg.id" :class="['message', msg.role]">
              <div class="avatar">{{ msg.role === 'user' ? '👤' : '🤖' }}</div>
              <div class="message-content">{{ msg.content }}</div>
            </div>
            <div v-if="chatLoading" class="loading-message">
              <span class="loading-dots">AI 正在思考...</span>
            </div>
          </div>
          <div class="input-area">
            <textarea 
              v-model="chatInput" 
              @keyup.enter.exact="handleSendChat" 
              placeholder="输入消息，按 Enter 发送..."
              :disabled="chatLoading"
            ></textarea>
            <button class="btn btn-primary" @click="handleSendChat()" :disabled="chatLoading">发送</button>
          </div>
        </div>
      </div>

      <!-- 智能助手 -->
      <div v-if="activeMainTab === 'auto'" class="panel">
        <div class="chat-container">
          <div class="chat-messages" ref="autoMessagesRef">
            <div v-for="msg in autoMessagesList" :key="msg.id" :class="['message', msg.role]">
              <div class="avatar">{{ msg.role === 'user' ? '👤' : '🤖' }}</div>
              <div class="message-content">{{ msg.content }}</div>
            </div>
            <div v-if="autoLoading" class="loading-message">
              <span class="loading-dots">AI 正在思考...</span>
            </div>
          </div>
          <div class="input-area">
            <textarea 
              v-model="autoInput" 
              @keyup.enter.exact="handleSendAuto" 
              placeholder="描述你要解决的问题，我会自主推理并调用工具处理..."
              :disabled="autoLoading"
            ></textarea>
            <button class="btn btn-primary" @click="handleSendAuto()" :disabled="autoLoading">发送</button>
          </div>
        </div>

        <h3 class="section-title">可用工具</h3>
        <div class="tool-grid">
          <div class="tool-card" v-for="tool in AUTO_TOOLS" :key="tool.name">
            <div class="tool-name">{{ tool.emoji }} {{ tool.name }}</div>
            <div class="tool-desc">{{ tool.desc }}</div>
          </div>
        </div>
      </div>

      <!-- ReAct 推理 -->
      <div v-if="activeMainTab === 'react'" class="panel">
        <div class="agent-info">
          <div class="agent-info-card">
            <div class="agent-info-value">{{ reactSteps }}</div>
            <div class="agent-info-label">推理步数</div>
          </div>
          <div class="agent-info-card">
            <div class="agent-info-value">{{ reactTime }}</div>
            <div class="agent-info-label">执行耗时</div>
          </div>
        </div>

        <div class="input-area">
          <textarea 
            v-model="reactInput" 
            @keyup.enter.exact="handleSendReact" 
            placeholder="输入问题，让 Agent 自主推理..."
            :disabled="isReactRunning"
          ></textarea>
          <button class="btn btn-primary" @click="handleSendReact()" :disabled="isReactRunning">推理</button>
        </div>

        <div class="quick-questions">
          <span>尝试：</span>
          <button 
            v-for="question in QUICK_QUESTIONS" 
            :key="question.text"
            class="quick-question" 
            @click="reactInput = question.text"
          >
            {{ question.emoji }} {{ question.text }}
          </button>
        </div>

        <div class="react-tabs">
          <button 
            v-for="tab in REACT_TABS" 
            :key="tab.id"
            :class="['react-tab', { active: activeReactTab === tab.id }]"
            @click="activeReactTab = tab.id"
          >
            {{ tab.name }}
          </button>
        </div>

        <div v-show="activeReactTab === 'detail'" class="react-detail">
          <div class="reasoning-steps" ref="reasoningStepsRef">
            <div v-for="(step, index) in reasoningSteps" :key="index" :class="['step-card', step.type]">
              <div :class="['step-number', step.type]">
                <span class="step-icon">{{ step.icon }}</span> {{ index + 1 }}{{ step.isFinal ? ' - 最终答案' : '' }}
              </div>
              <div class="step-content" v-html="step.content"></div>
            </div>
            <div v-if="reasoningSteps.length === 0" class="empty-state">
              <div>🧠</div>
              <div>等待你的问题...</div>
              <div>Agent 会思考 → 执行工具 → 观察结果 → 重复直到完成</div>
            </div>
          </div>
        </div>

        <div v-show="activeReactTab === 'quick'" class="react-detail">
          <div :class="['response-box', reactAnswerClass]">{{ reactAnswer }}</div>
        </div>

        <!-- 确认对话框 -->
        <div v-if="showConfirmation" class="confirmation-overlay">
          <div class="confirmation-box">
            <div class="confirmation-icon">⚠️</div>
            <h3>危险操作确认</h3>
            <p>Agent 需要执行以下操作：</p>
            <div class="confirmation-details">
              <strong>操作类型:</strong> {{ confirmationData.toolName || '未知' }}<br>
              <strong>操作描述:</strong> {{ confirmationData.description || '' }}
            </div>
            <div class="confirmation-warning">
              ⚠️ 此操作可能无法撤销，请确认是否继续？
            </div>
            <div class="confirmation-buttons">
              <button class="confirm-btn" @click="confirmOperation(true)">确认执行</button>
              <button class="cancel-btn" @click="confirmOperation(false)">取消操作</button>
            </div>
          </div>
        </div>
      </div>

      <!-- RAG 问答 -->
      <div v-if="activeMainTab === 'rag'" class="panel">
        <div class="stats">
          <div class="stat-card">
            <div class="stat-value">{{ kbCount }}</div>
            <div class="stat-label">知识库文档</div>
          </div>
        </div>

        <div class="chat-container">
          <div class="chat-messages" ref="ragMessagesRef">
            <div v-for="msg in ragMessagesList" :key="msg.id" :class="['message', msg.role]">
              <div class="avatar">{{ msg.role === 'user' ? '👤' : '📚' }}</div>
              <div class="message-content">{{ msg.content }}</div>
            </div>
            <div v-if="ragLoading" class="loading-message">
              <span class="loading-dots">AI 正在检索...</span>
            </div>
          </div>
          <div class="input-area">
            <textarea 
              v-model="ragInput" 
              @keyup.enter.exact="handleSendRag" 
              placeholder="输入问题..."
              :disabled="ragLoading"
            ></textarea>
            <button class="btn btn-primary" @click="handleSendRag()" :disabled="ragLoading">提问</button>
          </div>
        </div>

        <h3 class="section-title">添加文档到知识库</h3>
        <div class="form-row">
          <div class="form-group">
            <label>文档标题</label>
            <input type="text" v-model="docTitle" placeholder="输入文档标题">
          </div>
          <div class="form-group">
            <label>文档内容</label>
            <textarea v-model="docContent" rows="3" placeholder="输入文档内容"></textarea>
          </div>
        </div>
        <div class="btn-row">
          <button class="btn btn-success" @click="handleAddDocument()">添加文档</button>
          <button class="btn btn-danger btn-sm" @click="handleClearKnowledge()">清空知识库</button>
        </div>
      </div>

      <!-- 文件管理 -->
      <div v-if="activeMainTab === 'files'" class="panel">
        <h3 class="section-title">📂 资源文件 <span class="count-badge">{{ filesListData.length }} 个</span></h3>
        <div class="btn-row">
          <button class="btn btn-primary btn-sm" @click="loadFilesList()">🔄 刷新列表</button>
          <button class="btn btn-success btn-sm" @click="handleVectorizeAll()">⚡ 向量化所有文件</button>
        </div>
        <div class="files-list" ref="filesListRef">
          <div v-if="filesLoading" class="loading">加载中...</div>
          <div v-else-if="filesListData.length === 0" class="empty-state">暂无文件</div>
          <template v-else>
            <div v-for="file in filesListData" :key="file" class="file-item enhanced">
              <div class="file-info">
                <span class="file-icon">{{ getFileIcon(file) }}</span>
                <span class="file-name">{{ file }}</span>
              </div>
              <div class="file-actions">
                <button class="btn btn-primary btn-sm" @click="handleVectorizeFile(file)">⚡ 向量化</button>
                <button class="btn btn-secondary btn-sm" @click="singleFile = file">选择</button>
              </div>
            </div>
          </template>
        </div>

        <h3 class="section-title">📝 向量化单个文件</h3>
        <div class="form-group">
          <label>文件名</label>
          <input type="text" v-model="singleFile" placeholder="输入文件名（如：华为手机产品信息.md）">
        </div>
        <div class="btn-row">
          <button class="btn btn-success" @click="handleVectorizeSingle()">⚡ 向量化文件</button>
          <button class="btn btn-secondary btn-sm" @click="singleFile = ''">清空</button>
        </div>
        <div v-if="filesResponse" :class="['response-box', filesResponseClass]">
          <div class="response-header">📤 执行结果</div>
          <pre class="response-content">{{ filesResponse }}</pre>
        </div>
      </div>

      <!-- Skills -->
      <div v-if="activeMainTab === 'skills'" class="panel">
        <h3 class="section-title">🎯 已加载的 Skills <span class="count-badge">{{ skillsList.length }} 个</span></h3>
        <div class="btn-row">
          <button class="btn btn-primary btn-sm" @click="loadSkills()">🔄 刷新列表</button>
          <button class="btn btn-success btn-sm" @click="reloadSkillsAPI()">📥 重新加载 Skills</button>
        </div>
        <div class="skills-grid" ref="skillCardsList">
          <div v-if="skillsLoading" class="loading">加载中...</div>
          <div v-else-if="skillsList.length === 0" class="empty-state">暂无可用的 Skills</div>
          <template v-else>
            <div v-for="skill in skillsList" :key="skill.name" class="skill-card enhanced">
              <div class="skill-header">
                <span class="skill-name">🎯 {{ skill.name }}</span>
                <button class="btn btn-xs btn-outline" @click="selectSkill(skill)">使用</button>
              </div>
              <div class="skill-meta">
                <span v-if="skill.trigger" class="skill-trigger">🔔 触发词: {{ skill.trigger }}</span>
              </div>
              <div class="skill-desc">{{ skill.description || '暂无描述' }}</div>
              <div v-if="skill.steps && skill.steps.length > 0" class="skill-steps">
                <div class="skill-steps-title">📋 工作流程 ({{ skill.steps.length }} 步)</div>
                <div class="steps-list">
                  <div v-for="step in skill.steps" :key="step.order" class="step-item">
                    <span class="step-order">{{ step.order }}</span>
                    <span class="step-name">{{ step.name }}</span>
                    <span class="step-tool">🔧 {{ step.toolName }}</span>
                  </div>
                </div>
              </div>
            </div>
          </template>
        </div>

        <h3 class="section-title">⚡ 自动执行 Skill</h3>
        <div class="form-group">
          <label>问题描述</label>
          <textarea v-model="skillAutoInput" rows="2" placeholder="例如：北京今天天气怎么样？"></textarea>
        </div>
        <div class="form-group">
          <label>参数 (JSON 格式，可选)</label>
          <textarea v-model="skillAutoParams" rows="2" placeholder='{"city": "北京"}'></textarea>
        </div>
        <button class="btn btn-primary" @click="handleExecuteSkillAuto()">🚀 自动执行</button>
        <div v-if="skillAutoResponse" v-html="skillAutoResponse" class="skill-result"></div>

        <h3 class="section-title">📋 执行指定 Skill</h3>
        <div class="form-group">
          <label>Skill 名称</label>
          <input type="text" v-model="skillName" placeholder="例如：weather-report">
        </div>
        <div class="form-group">
          <label>参数 (JSON 格式)</label>
          <textarea v-model="skillParams" rows="2" placeholder='{"city": "北京"}'></textarea>
        </div>
        <button class="btn btn-success" @click="handleExecuteSkillByName()">▶️ 执行 Skill</button>
        <div v-html="skillExecuteResponse" class="skill-result"></div>
      </div>

      <!-- 工具调用 -->
      <div v-if="activeMainTab === 'tools'" class="panel">
        <h3 class="section-title">🔧 MCP 工具 <span class="count-badge">{{ toolsList.length }} 个</span></h3>
        <div class="btn-row">
          <button class="btn btn-primary btn-sm" @click="loadTools()">🔄 刷新列表</button>
        </div>
        <div class="tool-grid" ref="mcpList">
          <div v-if="toolsLoading" class="loading">加载中...</div>
          <div v-else-if="toolsList.length === 0" class="empty-state">暂无工具</div>
          <template v-else>
            <div v-for="tool in toolsList" :key="tool.name" class="tool-card enhanced">
              <div class="tool-header">
                <div class="tool-name">🔧 {{ tool.name }}</div>
                <button class="btn btn-xs btn-outline" @click="selectTool(tool)">使用</button>
              </div>
              <div class="tool-desc">{{ tool.description || '暂无描述' }}</div>
              <div v-if="tool.parameters && tool.parameters.length > 0" class="tool-params">
                <div class="params-title">📋 参数列表</div>
                <div class="params-list">
                  <div v-for="param in tool.parameters" :key="param" class="param-item">{{ param }}</div>
                </div>
              </div>
              <div v-if="tool.example" class="tool-example">
                <div class="example-title">💡 示例</div>
                <pre class="example-code">{{ formatToolExample(tool.example) }}</pre>
              </div>
            </div>
          </template>
        </div>

        <h3 class="section-title">⚡ 执行工具</h3>
        <div class="form-group">
          <label>工具名称</label>
          <input type="text" v-model="toolName" placeholder="输入工具名称">
        </div>
        <div class="form-group">
          <label>参数 (JSON 格式)</label>
          <textarea v-model="toolArgs" rows="3" placeholder='{"参数名": "参数值"}'></textarea>
        </div>
        <div class="btn-row">
          <button class="btn btn-primary" @click="handleExecuteTool()">▶️ 执行</button>
          <button class="btn btn-secondary btn-sm" @click="toolName = ''; toolArgs = '{}'">清空</button>
        </div>
        <div v-if="toolResponse" :class="['response-box', toolResponseClass]">
          <div class="response-header">📤 执行结果</div>
          <pre class="response-content">{{ toolResponse }}</pre>
        </div>
      </div>

      <!-- MCP 插件 -->
      <div v-if="activeMainTab === 'plugins'" class="panel">
        <h3 class="section-title">🔌 MCP 插件治理 <span class="count-badge">{{ governancePlugins.length }} 个</span></h3>
        <p class="description">插件生命周期管理：版本控制、状态管理、依赖检查</p>

        <div class="stats">
          <div class="stat-card" style="background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);">
            <div class="stat-value">{{ governanceStats.active }}</div>
            <div class="stat-label">活跃插件</div>
          </div>
          <div class="stat-card" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);">
            <div class="stat-value">{{ governanceStats.disabled }}</div>
            <div class="stat-label">已禁用</div>
          </div>
          <div class="stat-card">
            <div class="stat-value">{{ governanceStats.total }}</div>
            <div class="stat-label">总计</div>
          </div>
        </div>

        <h3 class="section-title">📦 插件列表</h3>
        <div class="btn-row">
          <button class="btn btn-primary btn-sm" @click="loadGovernancePlugins()">🔄 刷新</button>
          <select v-model="selectedPluginJar" class="plugin-select">
            <option value="">-- 选择插件 JAR --</option>
            <option value="D:\agentDemo\mcp-plugins\weather-plugin\target\weather-plugin-1.0.0.jar">天气插件</option>
            <option value="D:\agentDemo\mcp-plugins\mysql-plugin\target\mysql-plugin-1.0.0.jar">MySQL 插件</option>
            <option value="D:\agentDemo\mcp-plugins\filesystem-plugin\target\filesystem-plugin-1.0.0.jar">文件系统插件</option>
          </select>
          <button class="btn btn-success btn-sm" @click="handleLoadPlugin()">📥 加载插件</button>
        </div>
        <div class="tool-grid">
          <div v-if="governanceLoading" class="loading">加载中...</div>
          <div v-else-if="governancePlugins.length === 0" class="empty-state">暂无插件</div>
          <template v-else>
            <div v-for="plugin in governancePlugins" :key="plugin.id" class="plugin-card enhanced" :class="plugin.status?.toLowerCase()">
              <div class="plugin-header">
                <div class="plugin-name">🔌 {{ plugin.name }}</div>
                <span class="status-badge" :class="plugin.status?.toLowerCase()">{{ plugin.status || 'UNKNOWN' }}</span>
              </div>
              <div class="plugin-meta">
                <span v-if="plugin.version" class="plugin-version">🏷️ v{{ plugin.version }}</span>
                <span v-if="plugin.author" class="plugin-author">👤 {{ plugin.author }}</span>
              </div>
              <div class="plugin-desc">{{ plugin.description || '暂无描述' }}</div>
              <div v-if="plugin.serverNames && plugin.serverNames.length > 0" class="plugin-servers">
                <span class="servers-label">🖥️ Servers:</span>
                <div class="servers-list">
                  <span v-for="name in plugin.serverNames" :key="name" class="server-tag">{{ name }}</span>
                </div>
              </div>
              <div class="plugin-actions">
                <button v-if="plugin.status === 'ACTIVE'" class="btn btn-danger btn-sm" @click="disablePlugin(plugin.id)">🚫 禁用</button>
                <button v-else class="btn btn-success btn-sm" @click="enablePlugin(plugin.id)">✅ 启用</button>
                <button class="btn btn-primary btn-sm" @click="checkPluginCompatibility(plugin.id, plugin.version)">🔍 检查兼容性</button>
              </div>
            </div>
          </template>
        </div>

        <h3 class="section-title">🔍 版本比较工具</h3>
        <div class="form-row">
          <div class="form-group">
            <label>版本 1</label>
            <input type="text" v-model="version1" placeholder="如: 1.0.0" value="1.0.0">
          </div>
          <div class="form-group">
            <label>版本 2</label>
            <input type="text" v-model="version2" placeholder="如: 2.0.0" value="2.0.0">
          </div>
        </div>
        <button class="btn btn-primary btn-sm" @click="compareVersions()">🔍 比较版本</button>
        <div v-if="versionCompareResult" :class="['response-box', versionCompareClass]">
          <div class="response-header">📤 比较结果</div>
          <pre class="response-content">{{ versionCompareResult }}</pre>
        </div>

        <h3 class="section-title">📋 已注册的 Servers <span class="count-badge">{{ serversListData.length }} 个</span></h3>
        <div class="btn-row">
          <button class="btn btn-primary btn-sm" @click="loadServers()">🔄 刷新</button>
        </div>
        <div class="tool-grid">
          <div v-if="serversLoading" class="loading">加载中...</div>
          <div v-else-if="serversListData.length === 0" class="empty-state">暂无注册的 Server</div>
          <template v-else>
            <div v-for="server in serversListData" :key="server.name || server" class="server-card enhanced">
              <div class="tool-name">🖥️ {{ server.name || server }}</div>
              <div class="tool-desc">类型: {{ server.type || 'unknown' }}</div>
              <div class="btn-row">
                <button class="btn btn-danger btn-sm" @click="unregisterServer(server.name || server)">注销</button>
              </div>
            </div>
          </template>
        </div>

        <h3 class="section-title">🔧 可用 MCP 工具 <span class="count-badge">{{ mcpToolsListData.length }} 个</span></h3>
        <div class="tool-list-container">
          <div v-if="mcpToolsListData.length === 0" class="empty-state">请先加载插件</div>
          <div v-else class="tool-grid">
            <div v-for="tool in mcpToolsListData" :key="tool.name" class="tool-card">
              <div class="tool-name">🛠️ {{ tool.name }}</div>
              <div class="tool-desc">{{ tool.description || '' }}</div>
              <div v-if="getToolParams(tool)" class="tool-params">参数:<br>{{ getToolParams(tool) }}</div>
              <button class="btn btn-primary btn-sm" @click="mcpToolName = tool.name">使用</button>
            </div>
          </div>
        </div>

        <h3 class="section-title">⚡ 执行 MCP 工具</h3>
        <div class="form-group">
          <label>工具名称</label>
          <input type="text" v-model="mcpToolName" value="mysql_query">
        </div>
        <div class="form-group">
          <label>参数 (JSON 格式)</label>
          <textarea v-model="mcpToolArgs" rows="2" placeholder='{"sql": "SELECT * FROM users LIMIT 10"}'></textarea>
        </div>
        <button class="btn btn-primary" @click="executeMcpTool()">执行</button>
        <div :class="['response-box', mcpToolResponseClass]">{{ mcpToolResponse }}</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, computed } from 'vue'
import { javachainApi } from '../api/javachain'

// ==================== 类型定义 ====================
interface Message {
  id: number | string
  role: 'user' | 'assistant'
  content: string
}

interface Skill {
  name: string
  trigger?: string
  description?: string
  steps?: Array<{
    order: number
    name: string
    toolName: string
  }>
}

interface Tool {
  name: string
  description?: string
  parameters?: string[]
  example?: string
  inputSchema?: {
    properties?: Record<string, { description?: string }>
    required?: string[]
  }
}

interface Plugin {
  id: string
  name: string
  version?: string
  author?: string
  description?: string
  status?: string
  serverNames?: string[]
}

interface Server {
  name: string
  type?: string
}

interface ReasoningStep {
  type: 'thought' | 'action' | 'observation' | 'final'
  icon: string
  content: string
  isFinal: boolean
}

interface ConfirmationData {
  toolName?: string
  description?: string
}

interface GovernanceStats {
  active: number
  disabled: number
  total: number
}

// ==================== 常量定义 ====================
const MAIN_TABS = [
  { id: 'chat', name: '💬 简单对话' },
  { id: 'auto', name: '🤖 智能助手' },
  { id: 'react', name: '🧠 ReAct 推理' },
  { id: 'rag', name: '📚 RAG 问答' },
  { id: 'files', name: '📁 文件管理' },
  { id: 'skills', name: '🎯 Skills' },
  { id: 'tools', name: '🛠️ 工具调用' },
  { id: 'plugins', name: '🔌 MCP 插件' }
]

const REACT_TABS = [
  { id: 'detail', name: '📊 详细过程' },
  { id: 'quick', name: '⚡ 快速答案' }
]

const AUTO_TOOLS = [
  { name: '自主分析', desc: '理解用户问题，拆解目标并规划解决步骤', emoji: '🧠' },
  { name: '工具调用', desc: '按需调用 MCP、业务查询、知识库等工具获取证据', emoji: '🛠️' },
  { name: '结果整合', desc: '基于推理过程和工具结果生成最终答案', emoji: '✅' },
  { name: '安全确认', desc: '遇到高风险操作时先等待用户确认', emoji: '🛡️' }
]

const QUICK_QUESTIONS = [
  { text: '今天是哪一年？', emoji: '📅' },
  { text: '生成一个随机数，然后反转它', emoji: '🎲' },
  { text: '计算 5 + 10 × 2', emoji: '🧮' },
  { text: '北京今天的天气怎么样', emoji: '🌤️' }
]

// ==================== 状态管理 ====================
// 主标签页
const activeMainTab = ref<string>('chat')

// ReAct 子标签
const activeReactTab = ref<string>('detail')

// 简单对话
const currentSessionId = ref<string>('')
const chatInput = ref<string>('')
const chatMessagesList = ref<Message[]>([
  { id: 1, role: 'assistant', content: '你好！我是 DeepSeek AI，有什么可以帮你的？' }
])
const chatLoading = ref<boolean>(false)

// 智能助手
const autoInput = ref<string>('')
const autoMessagesList = ref<Message[]>([
  { id: 1, role: 'assistant', content: '你好！我是智能助手，重点是帮你解决问题。\n\n你可以直接描述目标或问题，我会自主分析、按需调用工具，并给出可落地的结果。' }
])
const autoLoading = ref<boolean>(false)

// ReAct 推理
const reactInput = ref<string>('')
const reactSteps = ref<number>(0)
const reactTime = ref<string>('0ms')
const reactAnswer = ref<string>('等待推理...')
const reactAnswerClass = ref<string>('')
const reasoningSteps = ref<ReasoningStep[]>([])
const showConfirmation = ref<boolean>(false)
const confirmationData = ref<ConfirmationData>({})
const currentJobId = ref<string | null>(null)
let pollingTimer: ReturnType<typeof setTimeout> | null = null
let isConfirmed = false

const isReactRunning = computed(() => showConfirmation.value || reasoningSteps.value.length > 0 && !reasoningSteps.value[reasoningSteps.value.length - 1]?.isFinal)

// RAG
const ragInput = ref<string>('')
const ragMessagesList = ref<Message[]>([
  { id: 1, role: 'assistant', content: '你好！我可以基于知识库回答你的问题。请先添加文档到知识库。' }
])
const kbCount = ref<string>('0')
const docTitle = ref<string>('')
const docContent = ref<string>('')
const ragLoading = ref<boolean>(false)

// 文件管理
const filesListData = ref<string[]>([])
const filesLoading = ref<boolean>(true)
const singleFile = ref<string>('')
const filesResponse = ref<string>('')
const filesResponseClass = ref<string>('')

// Skills
const skillsList = ref<Skill[]>([])
const skillsLoading = ref<boolean>(true)
const skillAutoInput = ref<string>('')
const skillAutoParams = ref<string>('')
const skillAutoResponse = ref<string>('')
const skillName = ref<string>('')
const skillParams = ref<string>('')
const skillExecuteResponse = ref<string>('')

// 工具调用
const toolsList = ref<Tool[]>([])
const toolsLoading = ref<boolean>(true)
const toolName = ref<string>('')
const toolArgs = ref<string>('')
const toolResponse = ref<string>('')
const toolResponseClass = ref<string>('')

// 插件管理
const governancePlugins = ref<Plugin[]>([])
const governanceLoading = ref<boolean>(true)
const governanceStats = ref<GovernanceStats>({ active: 0, disabled: 0, total: 0 })
const selectedPluginJar = ref<string>('')
const serversListData = ref<Server[]>([])
const serversLoading = ref<boolean>(true)
const mcpToolsListData = ref<Tool[]>([])
const version1 = ref<string>('1.0.0')
const version2 = ref<string>('2.0.0')
const versionCompareResult = ref<string>('')
const versionCompareClass = ref<string>('')
const mcpToolName = ref<string>('mysql_query')
const mcpToolArgs = ref<string>('')
const mcpToolResponse = ref<string>('')
const mcpToolResponseClass = ref<string>('')

// DOM refs
const chatMessagesRef = ref<HTMLElement | null>(null)
const autoMessagesRef = ref<HTMLElement | null>(null)
const reasoningStepsRef = ref<HTMLElement | null>(null)
const ragMessagesRef = ref<HTMLElement | null>(null)
const filesListRef = ref<HTMLElement | null>(null)

// ==================== 工具方法 ====================
const escapeHtml = (text: string): string => {
  if (!text) return ''
  const div = document.createElement('div')
  div.textContent = text
  return div.innerHTML
}

/**
 * 安全获取API响应数据
 * @param response Axios响应对象（已被拦截器提取response.data）
 * @returns 包含 success, data, message 的对象
 */
const safeApiResponse = (response: any) => {
  if (!response) {
    return { success: false, data: null, message: '响应为空' }
  }
  
  // 检查是否是标准响应格式（后端返回 {code, message, data, success}）
  const hasCode = 'code' in response
  const hasSuccess = 'success' in response
  
  // 优先使用 success 字段判断，其次使用 code === 200
  const success = response.success === true || (hasCode && response.code === 200)
  
  // 后端可能在 data 字段或 message 字段返回数据
  let data = response.data || null
  let message = response.message || ''
  
  // 如果 data 为空但 message 有内容，尝试将 message 作为数据
  // 这是因为某些后端接口将实际数据放在 message 字段中
  if (!data && message) {
    // 尝试解析 message 为 JSON
    try {
      const parsed = JSON.parse(message)
      if (Array.isArray(parsed) || (typeof parsed === 'object' && parsed !== null)) {
        data = parsed
        message = ''
      }
    } catch {
      // 不是 JSON，保持原样
    }
  }
  
  return { success, data, message }
}

const extractMessageContent = (data: any, fallback: string = ''): string => {
  if (!data) return fallback
  if (typeof data === 'string') return data
  if (typeof data === 'object') {
    return String(
      (data as Record<string, unknown>).content 
        || (data as Record<string, unknown>).result 
        || (data as Record<string, unknown>).response
        || (data as Record<string, unknown>).answer
        || JSON.stringify(data, null, 2)
    )
  }
  return String(data)
}

const formatAutoReasoningResult = (data: any, fallback: string = ''): string => {
  if (!data || typeof data !== 'object') {
    return extractMessageContent(data, fallback)
  }

  if (data.status === 'pending_confirmation') {
    return [
      '需要你确认后才能继续执行。',
      '',
      `工具: ${data.toolName || '未知工具'}`,
      `操作: ${data.description || '无描述'}`,
      data.actionInput ? `参数: ${JSON.stringify(data.actionInput, null, 2)}` : '',
      '',
      '请到“ReAct 推理”页签查看并确认该任务。'
    ].filter(Boolean).join('\n')
  }

  const answer = data.answer || fallback || '任务已完成'
  const meta = [
    data.status ? `状态: ${data.status === 'completed' ? '已完成' : data.status}` : '',
    typeof data.steps !== 'undefined' ? `推理步数: ${data.steps}` : '',
    typeof data.duration !== 'undefined' ? `耗时: ${data.duration}ms` : ''
  ].filter(Boolean)

  return meta.length > 0
    ? `${answer}\n\n${meta.join(' · ')}`
    : String(answer)
}

const parseJsonData = (data: any): any => {
  if (!data) return null
  if (typeof data === 'string') {
    try {
      const parsed = JSON.parse(data)
      return parsed
    } catch {
      return data
    }
  }
  return data
}

const formatSkillResult = (result: Record<string, unknown>): string => {
  const success = result.success ? '✅ 成功' : '❌ 失败'
  const duration = result.duration ? `${result.duration}ms` : ''
  const skillName = (result.skillName as string) || 'unknown'
  
  let html = `<div class="skill-result">
    <div class="skill-result-header">
      <span class="skill-result-name">🎯 Skill: ${escapeHtml(skillName)}</span>
      <span class="skill-result-status">${success}</span>
      <span class="skill-result-duration">⏱️ ${duration}</span>
    </div>`

  if (result.errorMessage) {
    html += `<div class="skill-result-error">错误: ${escapeHtml(result.errorMessage as string)}</div>`
  }

  if (result.stepResults && Array.isArray(result.stepResults)) {
    html += `<div class="skill-steps-result">
      <div class="skill-steps-result-title">执行步骤</div>`
    
    (result.stepResults as Array<Record<string, unknown>>).forEach((step) => {
      const stepStatus = step.success ? '✅' : '❌'
      html += `<div class="skill-step-result ${step.success ? 'success' : 'error'}">
        <div class="step-header">${stepStatus} 步骤 ${step.stepOrder}: ${escapeHtml(step.toolName as string)} (${step.duration}ms)</div>`
      
      if (step.success) {
        let output = (step.output as string) || ''
        if (output.length > 200) output = output.substring(0, 200) + '...'
        html += `<div class="step-output">${escapeHtml(output)}</div>`
      } else {
        html += `<div class="step-error">${escapeHtml((step.error as string) || '')}</div>`
      }
      html += '</div>'
    })
    html += '</div>'
  }
  html += '</div>'
  return html
}

const parseReasoningHistory = (history: string): ReasoningStep[] => {
  const steps: ReasoningStep[] = []
  const lines = history.split('\n')
  let stepNum = 0

  for (const line of lines) {
    if (line.includes('思考')) {
      stepNum++
      steps.push({
        type: 'thought',
        icon: '💡',
        content: `<strong>思考:</strong> ${line.replace(/思考.*?:/, '').trim()}`,
        isFinal: false
      })
    } else if (line.includes('行动')) {
      steps.push({
        type: 'action',
        icon: '🔧',
        content: `<strong>行动:</strong> ${line.replace(/行动.*?:/, '').trim()}`,
        isFinal: false
      })
    } else if (line.includes('观察')) {
      steps.push({
        type: 'observation',
        icon: '👁️',
        content: `<strong>观察:</strong> ${line.replace(/观察.*?:/, '').trim()}`,
        isFinal: false
      })
    } else if (line.includes('最终答案')) {
      steps.push({
        type: 'final',
        icon: '✅',
        content: `<strong>最终答案:</strong> ${line.replace(/最终答案.*?:/, '').trim()}`,
        isFinal: true
      })
    }
  }
  return steps
}

const parseReasoningSteps = (steps: any[]): ReasoningStep[] => {
  const result: ReasoningStep[] = []
  
  for (const step of steps) {
    if (step.isFinalAnswer || step.answerContent) {
      result.push({
        type: 'final',
        icon: '✅',
        content: `<strong>最终答案:</strong> ${step.answerContent || step.thought || ''}`,
        isFinal: true
      })
    } else {
      if (step.thought) {
        result.push({
          type: 'thought',
          icon: '💡',
          content: `<strong>思考:</strong> ${step.thought}`,
          isFinal: false
        })
      }
      if (step.action) {
        result.push({
          type: 'action',
          icon: '🔧',
          content: `<strong>行动:</strong> ${step.action}${step.actionInput ? `<br><strong>输入:</strong> ${step.actionInput}` : ''}`,
          isFinal: false
        })
      }
      if (step.observation) {
        result.push({
          type: 'observation',
          icon: '👁️',
          content: `<strong>观察:</strong> ${step.observation}`,
          isFinal: false
        })
      }
      if (step.errorMessage) {
        result.push({
          type: 'error',
          icon: '❌',
          content: `<strong>错误:</strong> ${step.errorMessage}`,
          isFinal: false
        })
      }
    }
  }
  
  return result
}

const getPluginStatusColor = (status?: string): string => {
  switch (status) {
    case 'ACTIVE': return '#10b981'
    case 'DISABLED': return '#ef4444'
    default: return '#6b7280'
  }
}

const getToolParams = (tool: Tool): string => {
  if (!tool.inputSchema?.properties) return ''
  return Object.entries(tool.inputSchema.properties)
    .map(([key, value]) => `• ${key}: ${value.description || '无描述'}`)
    .join('\n')
}

const formatToolExample = (example: string): string => {
  if (!example) return ''
  try {
    const parsed = JSON.parse(example)
    return JSON.stringify(parsed, null, 2)
  } catch {
    return example
  }
}

const selectTool = (tool: Tool) => {
  toolName.value = tool.name
  if (tool.example) {
    try {
      const parsed = JSON.parse(tool.example)
      if (parsed.arguments) {
        toolArgs.value = JSON.stringify(parsed.arguments, null, 2)
      } else {
        toolArgs.value = JSON.stringify(parsed, null, 2)
      }
    } catch {
      toolArgs.value = '{}'
    }
  } else {
    toolArgs.value = '{}'
  }
}

const selectSkill = (skill: Skill) => {
  skillName.value = skill.name
  skillParams.value = '{}'
}

const getFileIcon = (filename: string): string => {
  const ext = filename.split('.').pop()?.toLowerCase() || ''
  const icons: Record<string, string> = {
    'md': '📝',
    'txt': '📄',
    'pdf': '📕',
    'doc': '📘',
    'docx': '📘',
    'xls': '📗',
    'xlsx': '📗',
    'csv': '📊',
    'json': '📋',
    'xml': '📋',
    'html': '🌐',
    'css': '🎨',
    'js': '⚡',
    'ts': '💎',
    'java': '☕',
    'py': '🐍',
    'go': '🔵',
    'rs': '🦀',
    'png': '🖼️',
    'jpg': '🖼️',
    'jpeg': '🖼️',
    'gif': '🖼️',
    'svg': '🎭',
    'zip': '📦',
    'tar': '📦',
    'gz': '📦'
  }
  return icons[ext] || '📁'
}

// ==================== 滚动到底部 ====================
const scrollToBottom = (refEl: HTMLElement | null) => {
  nextTick(() => {
    if (refEl) {
      refEl.scrollTop = refEl.scrollHeight
    }
  })
}

// ==================== 完成推理结果处理 ====================
const finishResult = (data: Record<string, unknown>, startTime: number) => {
  const stepsCount = Array.isArray(data.steps) ? data.steps.length : (data.steps as number) || 0
  reactSteps.value = stepsCount
  reactTime.value = ((data.duration as number) || Date.now() - startTime) + 'ms'
  reactAnswer.value = (data.answer as string) || '推理完成'
  reactAnswerClass.value = data.status === 'completed' || data.success ? 'success' : 'error'
  
  if (Array.isArray(data.steps) && data.steps.length > 0) {
    reasoningSteps.value = parseReasoningSteps(data.steps)
    scrollToBottom(reasoningStepsRef.value)
  } else if (data.history) {
    reasoningSteps.value = parseReasoningHistory(data.history as string)
    scrollToBottom(reasoningStepsRef.value)
  }
}

// ==================== 启动轮询任务状态 ====================
const startPolling = (jobId: string, startTime: number, resetConfirmed: boolean = true) => {
  if (resetConfirmed) {
    isConfirmed = false
  }
  const poll = async () => {
    try {
      const response = await javachainApi.getJobStatus(jobId)
      const { success, data } = safeApiResponse(response)
      if (success && data) {
        if (data.status === 'pending_confirmation') {
          if (!isConfirmed) {
            confirmationData.value = data
            showConfirmation.value = true
          }
        } else if (['completed', 'failed', 'cancelled'].includes(data.status)) {
          finishResult(data, startTime)
          return
        }
        pollingTimer = setTimeout(poll, 1000)
      }
    } catch (error) {
      console.error('轮询失败:', error)
    }
  }
  poll()
}

// ==================== 停止轮询 ====================
const stopPolling = () => {
  if (pollingTimer) {
    clearTimeout(pollingTimer)
    pollingTimer = null
  }
}

// ==================== 简单对话相关 ====================
const createNewSession = async () => {
  try {
    const response = await javachainApi.createSession()
    const { success, data, message } = safeApiResponse(response)
    if (success && data) {
      currentSessionId.value = data.sessionId
      localStorage.setItem('chat-session-id', currentSessionId.value)
      chatMessagesList.value = [{ id: Date.now(), role: 'assistant', content: '你好！我是 DeepSeek AI，有什么可以帮你的？' }]
    }
  } catch (error) {
    console.error('创建会话失败:', error)
  }
}

const clearCurrentSession = async () => {
  if (!currentSessionId.value) return
  try {
    const response = await javachainApi.clearSession(currentSessionId.value)
    const { success } = safeApiResponse(response)
    if (success) {
      chatMessagesList.value = [{ id: Date.now(), role: 'assistant', content: '你好！我是 DeepSeek AI，有什么可以帮你的？' }]
    }
  } catch (error) {
    console.error('清空会话失败:', error)
  }
}

const handleSendChat = async () => {
  const message = chatInput.value.trim()
  if (!message) return
  
  chatMessagesList.value.push({ id: Date.now(), role: 'user', content: message })
  chatInput.value = ''
  chatLoading.value = true
  scrollToBottom(chatMessagesRef.value)
  
  if (!currentSessionId.value) {
    await createNewSession()
  }
  
  try {
    const response = await javachainApi.sendMessage(currentSessionId.value, message)
    const { success, data, message: msg } = safeApiResponse(response)
    if (success) {
      chatMessagesList.value.push({ 
        id: Date.now(), 
        role: 'assistant', 
        content: extractMessageContent(data, msg) 
      })
    } else {
      chatMessagesList.value.push({ 
        id: Date.now(), 
        role: 'assistant', 
        content: '错误: ' + (msg || '操作失败') 
      })
    }
  } catch (error) {
    chatMessagesList.value.push({ 
      id: Date.now(), 
      role: 'assistant', 
      content: '错误: ' + (error as Error).message 
    })
  } finally {
    chatLoading.value = false
    scrollToBottom(chatMessagesRef.value)
  }
}

// ==================== 智能助手相关 ====================
const handleSendAuto = async () => {
  const question = autoInput.value.trim()
  if (!question) return
  
  autoMessagesList.value.push({ id: Date.now(), role: 'user', content: question })
  autoInput.value = ''
  autoLoading.value = true
  scrollToBottom(autoMessagesRef.value)
  
  try {
    const response = await javachainApi.sendAutoMessage(question)
    const { success, data, message: msg } = safeApiResponse(response)
    if (success) {
      autoMessagesList.value.push({ 
        id: Date.now(), 
        role: 'assistant', 
        content: formatAutoReasoningResult(data, msg)
      })
    } else {
      autoMessagesList.value.push({ 
        id: Date.now(), 
        role: 'assistant', 
        content: '错误: ' + (msg || '操作失败') 
      })
    }
  } catch (error) {
    autoMessagesList.value.push({ 
      id: Date.now(), 
      role: 'assistant', 
      content: '错误: ' + (error as Error).message 
    })
  } finally {
    autoLoading.value = false
    scrollToBottom(autoMessagesRef.value)
  }
}

// ==================== ReAct 推理相关 ====================
const handleSendReact = async () => {
  const question = reactInput.value.trim()
  if (!question) return
  
  reactSteps.value = 0
  reactTime.value = '0ms'
  reasoningSteps.value = []
  reactAnswer.value = '正在推理中...'
  reactAnswerClass.value = ''
  
  const startTime = Date.now()
  
  try {
    const response = await javachainApi.startReact(question)
    const { success, data } = safeApiResponse(response)
    if (success && data) {
      if (data.status === 'pending_confirmation') {
        currentJobId.value = data.jobId
        confirmationData.value = data
        showConfirmation.value = true
        startPolling(data.jobId, startTime)
        return
      }
      finishResult(data, startTime)
    }
  } catch (error) {
    reactAnswer.value = '错误: ' + (error as Error).message
    reactAnswerClass.value = 'error'
  }
}

const confirmOperation = async (confirm: boolean) => {
  showConfirmation.value = false
  if (!currentJobId.value) return
  
  if (confirm) {
    isConfirmed = true
    reactAnswer.value = '用户已确认，继续执行...'
    reactAnswerClass.value = ''
    stopPolling()
    try {
      await javachainApi.confirmJob(currentJobId.value, true)
    } catch (error) {
      console.error('确认失败:', error)
    }
    setTimeout(() => {
      if (currentJobId.value) {
        startPolling(currentJobId.value, Date.now(), false)
      }
    }, 500)
  } else {
    stopPolling()
    try {
      await javachainApi.confirmJob(currentJobId.value, false)
    } catch (error) {
      console.error('取消失败:', error)
    }
    currentJobId.value = null
    reactAnswer.value = '用户已取消操作'
    reactAnswerClass.value = 'error'
  }
}

// ==================== RAG 相关 ====================
const handleSendRag = async () => {
  const question = ragInput.value.trim()
  if (!question) return
  
  ragMessagesList.value.push({ id: Date.now(), role: 'user', content: question })
  ragInput.value = ''
  ragLoading.value = true
  scrollToBottom(ragMessagesRef.value)
  
  try {
    const response = await javachainApi.queryKnowledge(question)
    const { success, data, message: msg } = safeApiResponse(response)
    if (success) {
      ragMessagesList.value.push({ 
        id: Date.now(), 
        role: 'assistant', 
        content: extractMessageContent(data, msg) 
      })
    } else {
      ragMessagesList.value.push({ 
        id: Date.now(), 
        role: 'assistant', 
        content: '错误: ' + (msg || '操作失败') 
      })
    }
  } catch (error) {
    ragMessagesList.value.push({ 
      id: Date.now(), 
      role: 'assistant', 
      content: '错误: ' + (error as Error).message 
    })
  } finally {
    ragLoading.value = false
    scrollToBottom(ragMessagesRef.value)
  }
}

const handleAddDocument = async () => {
  const title = docTitle.value || '未命名文档'
  const content = docContent.value
  
  if (!content) {
    alert('请输入文档内容')
    return
  }
  
  try {
    const response = await javachainApi.addDocument(title, content)
    const { success, message: msg } = safeApiResponse(response)
    if (success) {
      alert(msg || '文档添加成功！')
      docTitle.value = ''
      docContent.value = ''
      loadRagStats()
    } else {
      alert('添加失败: ' + (msg || '操作失败'))
    }
  } catch (error) {
    alert('添加失败: ' + (error as Error).message)
  }
}

const handleClearKnowledge = async () => {
  if (!confirm('确定要清空知识库吗？')) return
  
  try {
    const response = await javachainApi.clearKnowledgeBase()
    const { success, message: msg } = safeApiResponse(response)
    if (success) {
      alert(msg || '知识库已清空')
      ragMessagesList.value = [{ id: Date.now(), role: 'assistant', content: '知识库已清空，请添加新文档。' }]
      loadRagStats()
    } else {
      alert('清空失败: ' + (msg || '操作失败'))
    }
  } catch (error) {
    alert('清空失败: ' + (error as Error).message)
  }
}

const loadRagStats = async () => {
  try {
    const response = await javachainApi.getRagStats()
    const { success, data } = safeApiResponse(response)
    if (success && data) {
      if (typeof data === 'object') {
        kbCount.value = String((data as Record<string, unknown>).documentCount 
          || (data as Record<string, unknown>).count 
          || (data as Record<string, unknown>).total 
          || 0)
      } else {
        const match = String(data).match(/(\d+)/)
        kbCount.value = match ? match[1] : '0'
      }
    }
  } catch (error) {
    console.error('加载统计失败:', error)
  }
}

// ==================== 文件管理相关 ====================
const loadFilesList = async () => {
  filesLoading.value = true
  try {
    const response = await javachainApi.getFilesList()
    const { success, data } = safeApiResponse(response)
    if (success && Array.isArray(data)) {
      filesListData.value = data.map(item => {
        if (typeof item === 'object') {
          return (item as Record<string, unknown>).name 
            || (item as Record<string, unknown>).path 
            || (item as Record<string, unknown>).fileName
            || JSON.stringify(item)
        }
        return String(item)
      })
    }
  } catch (error) {
    console.error('加载文件列表失败:', error)
  } finally {
    filesLoading.value = false
  }
}

const handleVectorizeAll = async () => {
  filesResponse.value = '正在向量化...'
  filesResponseClass.value = ''
  
  try {
    const response = await javachainApi.vectorizeFiles()
    const { success, data, message: msg } = safeApiResponse(response)
    if (success) {
      filesResponse.value = data || msg
      filesResponseClass.value = 'success'
      loadRagStats()
    } else {
      filesResponse.value = '错误: ' + (msg || '操作失败')
      filesResponseClass.value = 'error'
    }
  } catch (error) {
    filesResponse.value = '错误: ' + (error as Error).message
    filesResponseClass.value = 'error'
  }
}

const handleVectorizeFile = async (filePath: string) => {
  singleFile.value = filePath
  filesResponse.value = `正在向量化 ${filePath}...`
  filesResponseClass.value = ''
  
  try {
    const response = await javachainApi.vectorizeSingleFile(filePath)
    const { success, data, message: msg } = safeApiResponse(response)
    if (success) {
      filesResponse.value = data || msg
      filesResponseClass.value = 'success'
      loadRagStats()
    } else {
      filesResponse.value = '错误: ' + (msg || '操作失败')
      filesResponseClass.value = 'error'
    }
  } catch (error) {
    filesResponse.value = '错误: ' + (error as Error).message
    filesResponseClass.value = 'error'
  }
}

const handleVectorizeSingle = async () => {
  const filePath = singleFile.value.trim()
  if (!filePath) {
    alert('请输入文件名')
    return
  }
  
  handleVectorizeFile(filePath)
}

// ==================== Skills 相关 ====================
const loadSkills = async () => {
  skillsLoading.value = true
  skillsList.value = []
  try {
    const response = await javachainApi.getSkills()
    console.log('🎯 Skills接口原始响应:', JSON.stringify(response, null, 2))
    
    const { success, data } = safeApiResponse(response)
    console.log('🎯 safeApiResponse 解析结果 - success:', success, 'data:', data)
    
    if (!success) {
      console.error('🎯 接口调用失败')
      return
    }
    
    if (!data) {
      console.warn('🎯 数据为空')
      return
    }
    
    const parsedData = parseJsonData(data)
    console.log('🎯 parseJsonData 结果:', parsedData, '类型:', typeof parsedData)
    
    if (Array.isArray(parsedData)) {
      skillsList.value = parsedData as Skill[]
    } else if (parsedData && typeof parsedData === 'object') {
      // 支持多种嵌套结构
      skillsList.value = (parsedData as Record<string, unknown>).skills as Skill[] || 
                        (parsedData as Record<string, unknown>).data as Skill[] || []
    }
    
    console.log('🎯 Skills列表加载完成，共', skillsList.value.length, '条')
    
  } catch (error) {
    console.error('🎯 加载 Skills 失败:', error)
  } finally {
    skillsLoading.value = false
  }
}

const reloadSkillsAPI = async () => {
  try {
    const response = await javachainApi.reloadSkills()
    const { success, message: msg } = safeApiResponse(response)
    if (success) {
      alert(msg || 'Skills 已重新加载')
      loadSkills()
    } else {
      alert('重新加载失败: ' + (msg || '未知错误'))
    }
  } catch (error) {
    alert('重新加载失败: ' + (error as Error).message)
  }
}

const handleExecuteSkillAuto = async () => {
  const userInput = skillAutoInput.value.trim()
  if (!userInput) {
    alert('请输入问题描述')
    return
  }
  
  let params: Record<string, unknown> = {}
  if (skillAutoParams.value) {
    try {
      params = JSON.parse(skillAutoParams.value)
    } catch {
      alert('参数 JSON 格式错误')
      return
    }
  }
  
  skillAutoResponse.value = '<div class="loading">⏳ 正在自动匹配并执行 Skill...</div>'
  
  try {
    const response = await javachainApi.executeSkillAuto(userInput, params)
    const { success, data, message: msg } = safeApiResponse(response)
    if (success && data) {
      skillAutoResponse.value = formatSkillResult(data)
    } else {
      skillAutoResponse.value = `<div style="color: #dc3545;">执行失败: ${escapeHtml(msg || '未知错误')}</div>`
    }
  } catch (error) {
    skillAutoResponse.value = `<div style="color: #dc3545;">执行失败: ${escapeHtml((error as Error).message)}</div>`
  }
}

const handleExecuteSkillByName = async () => {
  const name = skillName.value.trim()
  if (!name) {
    alert('请输入 Skill 名称')
    return
  }
  
  let params: Record<string, unknown> = {}
  if (skillParams.value) {
    try {
      params = JSON.parse(skillParams.value)
    } catch {
      alert('参数 JSON 格式错误')
      return
    }
  }
  
  skillExecuteResponse.value = `<div class="loading">⏳ 正在执行 Skill: ${escapeHtml(name)}...</div>`
  
  try {
    const response = await javachainApi.executeSkill(name, params)
    const { success, data, message: msg } = safeApiResponse(response)
    if (success && data) {
      skillExecuteResponse.value = formatSkillResult(data)
    } else {
      skillExecuteResponse.value = `<div style="color: #dc3545;">执行失败: ${escapeHtml(msg || '未知错误')}</div>`
    }
  } catch (error) {
    skillExecuteResponse.value = `<div style="color: #dc3545;">执行失败: ${escapeHtml((error as Error).message)}</div>`
  }
}

// ==================== 工具调用相关 ====================
const loadTools = async () => {
  toolsLoading.value = true
  toolsList.value = []
  try {
    const response = await javachainApi.getTools()
    console.log('🔧 工具列表接口原始响应:', JSON.stringify(response, null, 2))
    
    const { success, data } = safeApiResponse(response)
    console.log('🔧 safeApiResponse 解析结果 - success:', success, 'data:', data)
    
    if (!success) {
      console.error('🔧 接口调用失败')
      return
    }
    
    if (!data) {
      console.warn('🔧 数据为空')
      return
    }
    
    // 尝试解析数据
    const parsedData = parseJsonData(data)
    console.log('🔧 parseJsonData 结果:', parsedData, '类型:', typeof parsedData)
    
    if (Array.isArray(parsedData)) {
      toolsList.value = parsedData as Tool[]
    } else if (parsedData && typeof parsedData === 'object') {
      // 支持嵌套结构 { tools: [...] }
      toolsList.value = (parsedData as Record<string, unknown>).tools as Tool[] || 
                        (parsedData as Record<string, unknown>).data as Tool[] || []
    }
    
    console.log('🔧 工具列表加载完成，共', toolsList.value.length, '条')
    
  } catch (error) {
    console.error('🔧 加载工具失败:', error)
  } finally {
    toolsLoading.value = false
  }
}

const handleExecuteTool = async () => {
  const name = toolName.value.trim()
  if (!name) {
    alert('请输入工具名称')
    return
  }
  
  let args: Record<string, unknown> = {}
  if (toolArgs.value) {
    try {
      args = JSON.parse(toolArgs.value)
    } catch {
      toolResponse.value = 'JSON 格式错误'
      toolResponseClass.value = 'error'
      return
    }
  }
  
  // 危险操作确认
  if (name === 'filesystem_delete') {
    const filePath = (args.path as string) || '未知文件'
    const confirmed = confirm(`⚠️ 危险操作确认\n\n您即将删除文件: ${filePath}\n\n此操作无法撤销，确定要继续吗？`)
    if (!confirmed) {
      toolResponse.value = '操作已取消'
      toolResponseClass.value = 'error'
      return
    }
  }
  
  toolResponse.value = '执行中...'
  toolResponseClass.value = ''
  
  try {
    const response = await javachainApi.executeTool(name, args)
    const { success, data, message: msg } = safeApiResponse(response)
    if (success) {
      toolResponse.value = data || msg
      toolResponseClass.value = 'success'
    } else {
      toolResponse.value = '错误: ' + (msg || '操作失败')
      toolResponseClass.value = 'error'
    }
  } catch (error) {
    toolResponse.value = '错误: ' + (error as Error).message
    toolResponseClass.value = 'error'
  }
}

// ==================== 插件管理相关 ====================
const loadGovernancePlugins = async () => {
  governanceLoading.value = true
  try {
    const response = await javachainApi.getGovernancePlugins()
    const { success, data } = safeApiResponse(response)
    if (success && data) {
      const parsedData = parseJsonData(data)
      if (Array.isArray(parsedData)) {
        governancePlugins.value = parsedData as Plugin[]
        governanceStats.value = { 
          active: parsedData.filter((p: Plugin) => p.status === 'ACTIVE').length, 
          disabled: parsedData.filter((p: Plugin) => p.status === 'DISABLED').length, 
          total: parsedData.length 
        }
      } else if (parsedData && typeof parsedData === 'object') {
        governancePlugins.value = (parsedData as Record<string, unknown>).plugins as Plugin[] || []
        governanceStats.value = (parsedData as Record<string, unknown>).stats as GovernanceStats || { active: 0, disabled: 0, total: 0 }
      }
    }
  } catch (error) {
    console.error('加载插件治理失败:', error)
  } finally {
    governanceLoading.value = false
  }
}

const enablePlugin = async (pluginId: string) => {
  try {
    const response = await javachainApi.enablePlugin(pluginId)
    const { success, message: msg } = safeApiResponse(response)
    if (success) {
      alert('插件已启用')
      loadGovernancePlugins()
    } else {
      alert('启用失败: ' + msg)
    }
  } catch (error) {
    alert('启用失败: ' + (error as Error).message)
  }
}

const disablePlugin = async (pluginId: string) => {
  if (!confirm('确定要禁用此插件吗？')) return
  
  try {
    const response = await javachainApi.disablePlugin(pluginId)
    const { success, message: msg } = safeApiResponse(response)
    if (success) {
      alert('插件已禁用')
      loadGovernancePlugins()
    } else {
      alert('禁用失败: ' + msg)
    }
  } catch (error) {
    alert('禁用失败: ' + (error as Error).message)
  }
}

const checkPluginCompatibility = async (pluginId: string, version: string) => {
  try {
    const response = await javachainApi.checkPluginCompatibility(pluginId, version)
    const { success, data, message: msg } = safeApiResponse(response)
    if (success && data) {
      const dataObj = data as Record<string, unknown> || {}
      alert(`插件 ${dataObj.pluginId} 版本 ${dataObj.requestedVersion} 兼容性: ${dataObj.compatible ? '兼容' : '不兼容'}`)
    } else {
      throw new Error(msg || '检查失败')
    }
  } catch (error) {
    alert('检查失败: ' + (error as Error).message)
  }
}

const compareVersions = async () => {
  const v1 = version1.value.trim()
  const v2 = version2.value.trim()
  
  if (!v1 || !v2) {
    versionCompareResult.value = '请输入两个版本号'
    versionCompareClass.value = 'error'
    return
  }
  
  try {
    const response = await javachainApi.compareVersions(v1, v2)
    const { success, data, message: msg } = safeApiResponse(response)
    if (success && data) {
      const dataObj = data as Record<string, unknown> || {}
      versionCompareResult.value = `${dataObj.v1} ${dataObj.comparison} ${dataObj.v2}`
      versionCompareClass.value = 'success'
    } else {
      throw new Error(msg || '比较失败')
    }
  } catch (error) {
    versionCompareResult.value = '比较失败: ' + (error as Error).message
    versionCompareClass.value = 'error'
  }
}

const handleLoadPlugin = () => {
  const jarPath = selectedPluginJar.value
  if (!jarPath) {
    alert('请选择一个插件！')
    return
  }
  
  if (confirm(`确定要加载插件吗？`)) {
    loadPluginByPath(jarPath)
  }
}

const loadPluginByPath = async (jarPath: string) => {
  try {
    const response = await javachainApi.loadPlugin(jarPath)
    const { success, message: msg } = safeApiResponse(response)
    if (success) {
      alert('插件加载成功！')
      loadGovernancePlugins()
      loadServersInfo()
    } else {
      alert('插件加载失败: ' + (msg || '未知错误'))
    }
  } catch (error) {
    alert('加载失败: ' + (error as Error).message)
  }
}

const loadServersInfo = async () => {
  serversLoading.value = true
  try {
    const response = await javachainApi.getServers()
    const { success, data } = safeApiResponse(response)
    if (success && data) {
      const parsedData = parseJsonData(data)
      if (Array.isArray(parsedData)) {
        serversListData.value = parsedData as Server[]
      } else if (parsedData && typeof parsedData === 'object') {
        serversListData.value = (parsedData as Record<string, unknown>).servers as Server[] || []
      }
    }
  } catch (error) {
    console.error('加载服务器列表失败:', error)
  } finally {
    serversLoading.value = false
    loadMcpToolsList()
  }
}

const loadMcpToolsList = async () => {
  try {
    const response = await javachainApi.getTools()
    const { success, data } = safeApiResponse(response)
    if (success && data) {
      const parsedData = parseJsonData(data)
      if (Array.isArray(parsedData)) {
        mcpToolsListData.value = parsedData as Tool[]
      } else if (parsedData && typeof parsedData === 'object') {
        mcpToolsListData.value = (parsedData as Record<string, unknown>).tools as Tool[] || []
      }
    }
  } catch (error) {
    console.error('加载 MCP 工具失败:', error)
  }
}

const unregisterServer = async (serverName: string) => {
  if (!confirm(`确定要注销 Server "${serverName}" 吗？`)) return
  
  try {
    const response = await javachainApi.unregisterServer(serverName)
    const { success, message: msg } = safeApiResponse(response)
    if (success) {
      alert('Server 注销成功！')
      loadServersInfo()
    } else {
      alert('注销失败: ' + (msg || '未知错误'))
    }
  } catch (error) {
    alert('注销失败: ' + (error as Error).message)
  }
}

const executeMcpTool = async () => {
  const name = mcpToolName.value.trim()
  if (!name) {
    alert('请输入工具名称')
    return
  }
  
  let args: Record<string, unknown> = {}
  if (mcpToolArgs.value) {
    try {
      args = JSON.parse(mcpToolArgs.value)
    } catch {
      alert('参数 JSON 格式错误')
      return
    }
  }
  
  mcpToolResponse.value = '正在执行...'
  mcpToolResponseClass.value = ''
  
  try {
    const response = await javachainApi.executeMcpTool(name, args)
    const { success, data, message: msg } = safeApiResponse(response)
    if (success) {
      const dataObj = data as Record<string, unknown> || {}
      mcpToolResponse.value = (dataObj.content as string) || '执行成功，无返回内容'
      mcpToolResponseClass.value = 'success'
    } else {
      const dataObj = data as Record<string, unknown> || {}
      mcpToolResponse.value = '错误: ' + (msg || dataObj.content || '未知错误')
      mcpToolResponseClass.value = 'error'
    }
  } catch (error) {
    mcpToolResponse.value = '执行失败: ' + (error as Error).message
    mcpToolResponseClass.value = 'error'
  }
}

// ==================== 页面初始化 ====================
onMounted(() => {
  // 恢复会话
  currentSessionId.value = localStorage.getItem('chat-session-id') || ''
  
  // 加载各模块数据
  loadRagStats()
  loadFilesList()
  loadTools()
  loadSkills()
  loadServersInfo()
  loadGovernancePlugins()
})
</script>

<style scoped>
/* 主容器 */
.javachain-container {
  padding: 20px;
  width: 100%;
  height: 100vh;
  min-height: 100vh;
  margin: 0 auto;
  background: linear-gradient(135deg, #1e3a5f 0%, #2d5a87 50%, #4a7ca3 100%);
  box-sizing: border-box;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

/* 头部 */
.javachain-header {
  text-align: center;
  margin-bottom: 24px;
}

.javachain-header h1 {
  color: white;
  margin-bottom: 8px;
  font-size: 2.2rem;
  text-shadow: 2px 2px 4px rgba(0, 0, 0, 0.3);
}

.subtitle {
  color: rgba(255, 255, 255, 0.9);
  font-size: 14px;
}

/* 标签页 */
.tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
  flex-wrap: wrap;
  justify-content: center;
}

.tab {
  padding: 12px 20px;
  border: none;
  border-radius: 25px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.3s;
  background: rgba(255, 255, 255, 0.15);
  color: white;
  backdrop-filter: blur(10px);
}

.tab:hover {
  background: rgba(255, 255, 255, 0.25);
  transform: translateY(-2px);
}

.tab.active {
  background: white;
  color: #1e3a5f;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
}

/* 内容区域 */
.tab-content {
  display: none;
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
  flex: 1;
  overflow: hidden;
  box-sizing: border-box;
}

.tab-content.active {
  display: flex;
  flex-direction: column;
}

/* 面板 */
.panel {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

/* 会话栏 */
.session-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #f8f9fa;
  border-radius: 8px;
  margin-bottom: 16px;
}

.session-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.session-label {
  font-size: 13px;
  color: #6c757d;
}

.session-id {
  font-size: 13px;
  font-family: monospace;
  color: #2d5a87;
}

.session-actions {
  display: flex;
  gap: 8px;
}

/* 按钮样式 */
.btn {
  padding: 8px 16px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s;
}

.btn-primary {
  background: linear-gradient(135deg, #2d5a87 0%, #4a7ca3 100%);
  color: white;
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(45, 90, 135, 0.4);
}

.btn-secondary {
  background: #5a6c7d;
  color: white;
}

.btn-secondary:hover {
  background: #4a5c6d;
}

.btn-success {
  background: linear-gradient(135deg, #1d8b70 0%, #2ecc71 100%);
  color: white;
}

.btn-success:hover {
  transform: translateY(-2px);
}

.btn-danger {
  background: linear-gradient(135deg, #c0392b 0%, #e74c3c 100%);
  color: white;
}

.btn-danger:hover {
  transform: translateY(-2px);
}

.btn-sm {
  padding: 6px 12px;
  font-size: 12px;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* 聊天容器 */
.chat-container {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 300px;
  border: 1px solid #e9ecef;
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 16px;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background: #fafafa;
}

.message {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.message.user {
  flex-direction: row-reverse;
}

.message.user .message-content {
  background: linear-gradient(135deg, #2d5a87 0%, #4a7ca3 100%);
  color: white;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: #e9ecef;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
}

.message-content {
  max-width: 70%;
  padding: 12px 16px;
  border-radius: 16px;
  background: white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  font-size: 14px;
  line-height: 1.5;
}

.message.user .message-content {
  border-bottom-right-radius: 4px;
}

.message.assistant .message-content {
  border-bottom-left-radius: 4px;
}

/* 加载消息 */
.loading-message {
  display: flex;
  justify-content: center;
  padding: 16px;
}

.loading-dots {
  font-size: 14px;
  color: #2d5a87;
}

.loading-dots::after {
  content: '';
  animation: dots 1.5s infinite;
}

@keyframes dots {
  0% { content: ''; }
  25% { content: '.'; }
  50% { content: '..'; }
  75% { content: '...'; }
  100% { content: ''; }
}

/* 输入区域 */
.input-area {
  display: flex;
  gap: 12px;
  padding: 12px;
  border-top: 1px solid #e9ecef;
  background: white;
}

.input-area textarea {
  flex: 1;
  padding: 10px 14px;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  resize: none;
  font-size: 14px;
  line-height: 1.5;
  transition: border-color 0.2s;
}

.input-area textarea:focus {
  outline: none;
  border-color: #2d5a87;
}

.input-area textarea:disabled {
  background: #f8f9fa;
  cursor: not-allowed;
}

/* 统计卡片 */
.stats {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.stat-card {
  flex: 1;
  min-width: 140px;
  padding: 16px;
  border-radius: 12px;
  background: linear-gradient(135deg, #2d5a87 0%, #4a7ca3 100%);
  text-align: center;
  color: white;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 13px;
  opacity: 0.9;
}

/* 工具网格 */
.tool-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
  margin-top: 16px;
}

/* 工具列表容器 - 支持滚动 */
.tool-list-container {
  max-height: 400px;
  overflow-y: auto;
  margin-top: 16px;
  padding-right: 8px;
}

.tool-list-container::-webkit-scrollbar {
  width: 6px;
}

.tool-list-container::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.tool-list-container::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.tool-list-container::-webkit-scrollbar-thumb:hover {
  background: #a1a1a1;
}

.tool-card {
  padding: 16px;
  border-radius: 12px;
  background: #f8f9fa;
  border-left: 4px solid #2d5a87;
  transition: transform 0.2s;
}

.tool-card:hover {
  transform: translateY(-2px);
}

.tool-name {
  font-weight: bold;
  margin-bottom: 8px;
  color: #212529;
}

.tool-desc {
  font-size: 13px;
  color: #6c757d;
  margin-bottom: 8px;
}

.tool-params {
  font-size: 12px;
  color: #858585;
  white-space: pre-wrap;
}

.tool-example {
  font-size: 12px;
  color: #10b981;
  margin-top: 8px;
}

/* 表单样式 */
.form-row {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.form-group {
  flex: 1;
  min-width: 200px;
}

.form-group label {
  display: block;
  margin-bottom: 6px;
  font-weight: 500;
  font-size: 13px;
  color: #495057;
}

.form-group input,
.form-group textarea {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  font-size: 14px;
  transition: border-color 0.2s;
}

.form-group input:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #2d5a87;
}

.btn-row {
  display: flex;
  gap: 10px;
  margin-top: 16px;
  flex-wrap: wrap;
}

/* 响应框 */
.response-box {
  margin-top: 16px;
  padding: 12px 16px;
  border-radius: 8px;
  font-size: 14px;
  min-height: 40px;
}

.response-box.success {
  background: #d1fae5;
  color: #065f46;
}

.response-box.error {
  background: #fee2e2;
  color: #991b1b;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 40px;
  color: #6c757d;
}

.empty-state div:first-child {
  font-size: 48px;
  margin-bottom: 12px;
}

/* 加载状态 */
.loading {
  text-align: center;
  padding: 20px;
  color: #2d5a87;
}

/* Skills 卡片 */
.skills-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 16px;
  margin-top: 16px;
}

.skill-card {
  padding: 16px;
  border-radius: 12px;
  background: linear-gradient(135deg, #2d5a8710 0%, #4a7ca310 100%);
  border: 1px solid #2d5a8730;
}

.skill-header {
  margin-bottom: 8px;
}

.skill-name {
  font-weight: bold;
  color: #212529;
}

.skill-trigger {
  display: block;
  font-size: 12px;
  color: #2d5a87;
  margin-top: 4px;
}

.skill-desc {
  font-size: 13px;
  color: #6c757d;
  margin-bottom: 12px;
}

.skill-steps {
  background: rgba(0, 0, 0, 0.03);
  border-radius: 8px;
  padding: 12px;
}

.skill-steps-title {
  font-size: 12px;
  font-weight: bold;
  color: #495057;
  margin-bottom: 8px;
}

.skill-step-item {
  font-size: 12px;
  color: #6c757d;
  margin-bottom: 4px;
}

.skill-more {
  font-size: 11px;
  color: #858585;
  margin-top: 4px;
}

/* Skill 执行结果 */
.skill-result {
  margin-top: 16px;
  padding: 16px;
  border-radius: 8px;
  background: #f8f9fa;
}

.skill-result-header {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.skill-result-name {
  font-weight: bold;
  color: #212529;
}

.skill-result-status {
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
}

.skill-result-duration {
  font-size: 12px;
  color: #6c757d;
}

.skill-result-error {
  padding: 8px;
  background: #fee2e2;
  border-radius: 6px;
  color: #991b1b;
  font-size: 13px;
}

.skill-steps-result {
  margin-top: 12px;
}

.skill-steps-result-title {
  font-size: 12px;
  font-weight: bold;
  color: #495057;
  margin-bottom: 8px;
}

.skill-step-result {
  margin-bottom: 8px;
  padding: 10px;
  border-radius: 6px;
}

.skill-step-result.success {
  background: #d1fae5;
}

.skill-step-result.error {
  background: #fee2e2;
}

.step-header {
  font-size: 12px;
  font-weight: bold;
  margin-bottom: 6px;
}

.step-output {
  font-size: 13px;
  color: #495057;
}

.step-error {
  font-size: 13px;
  color: #991b1b;
}

/* ReAct 推理 */
.agent-info {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
}

.agent-info-card {
  flex: 1;
  padding: 20px;
  border-radius: 12px;
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
  text-align: center;
  color: white;
}

.agent-info-value {
  font-size: 32px;
  font-weight: bold;
  margin-bottom: 4px;
}

.agent-info-label {
  font-size: 13px;
  opacity: 0.9;
}

.quick-questions {
  margin-top: 16px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  align-items: center;
}

.quick-question {
  padding: 8px 14px;
  background: #f8f9fa;
  border: none;
  border-radius: 20px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.quick-question:hover {
  background: #e9ecef;
}

.react-tabs {
  display: flex;
  gap: 8px;
  margin-top: 20px;
}

.react-tab {
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  background: #f8f9fa;
  transition: all 0.2s;
}

.react-tab:hover {
  background: #e9ecef;
}

.react-tab.active {
  background: #2d5a87;
  color: white;
}

.react-detail {
  margin-top: 16px;
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.reasoning-steps {
  flex: 1;
  min-height: 300px;
  max-height: none;
  overflow-y: auto;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 12px;
}

.step-card {
  padding: 16px;
  border-radius: 10px;
  margin-bottom: 12px;
  animation: fadeIn 0.3s ease;
}

.step-card.thought {
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  border-left: 4px solid #f59e0b;
}

.step-card.action {
  background: linear-gradient(135deg, #dbeafe 0%, #bfdbfe 100%);
  border-left: 4px solid #3b82f6;
}

.step-card.observation {
  background: linear-gradient(135deg, #d1fae5 0%, #a7f3d0 100%);
  border-left: 4px solid #10b981;
}

.step-card.final {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  border-left: 4px solid #ec4899;
}

.step-number {
  font-weight: bold;
  margin-bottom: 8px;
  font-size: 13px;
}

.step-content {
  font-size: 14px;
  line-height: 1.6;
}

/* 确认对话框 */
.confirmation-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: fadeIn 0.2s ease;
}

.confirmation-box {
  background: white;
  border-radius: 16px;
  padding: 24px;
  max-width: 400px;
  width: 90%;
  animation: slideUp 0.3s ease;
}

@keyframes slideUp {
  from { transform: translateY(20px); opacity: 0; }
  to { transform: translateY(0); opacity: 1; }
}

.confirmation-icon {
  font-size: 48px;
  text-align: center;
  margin-bottom: 12px;
}

.confirmation-box h3 {
  margin: 0 0 12px 0;
  text-align: center;
  color: #212529;
}

.confirmation-box p {
  text-align: center;
  color: #6c757d;
  margin-bottom: 16px;
}

.confirmation-details {
  background: #f8f9fa;
  padding: 12px;
  border-radius: 8px;
  margin-bottom: 16px;
  font-size: 13px;
}

.confirmation-warning {
  background: #fff3cd;
  padding: 12px;
  border-radius: 8px;
  margin-bottom: 20px;
  color: #856404;
  font-size: 13px;
}

.confirmation-buttons {
  display: flex;
  gap: 12px;
}

.confirm-btn {
  flex: 1;
  padding: 12px;
  border: none;
  border-radius: 8px;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
  font-weight: 500;
  cursor: pointer;
  transition: transform 0.2s;
}

.confirm-btn:hover {
  transform: translateY(-1px);
}

.cancel-btn {
  flex: 1;
  padding: 12px;
  border: none;
  border-radius: 8px;
  background: #6c757d;
  color: white;
  font-weight: 500;
  cursor: pointer;
  transition: transform 0.2s;
}

.cancel-btn:hover {
  transform: translateY(-1px);
}

/* 文件列表 */
.files-list {
  margin-top: 16px;
}

.file-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
  margin-bottom: 8px;
}

.file-name {
  font-size: 14px;
  color: #212529;
}

/* 插件选择 */
.plugin-select {
  padding: 8px 12px;
  border: 1px solid #e9ecef;
  border-radius: 6px;
  font-size: 14px;
  background: white;
}

.status-badge {
  float: right;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 10px;
}

/* 文档标题 */
.section-title {
  margin: 20px 0 12px 0;
  font-size: 16px;
  color: #212529;
}

.description {
  color: #6c757d;
  font-size: 14px;
  margin-bottom: 16px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .javachain-container {
    width: 85%;
    padding: 12px;
  }
  
  .chat-container {
    flex: 1;
    min-height: 100%
  }
  
  .message-content {
    max-width: 85%;
  }
  
  .agent-info {
    flex-direction: column;
  }
  
  .form-row {
    flex-direction: column;
  }
  
  .btn-row {
    flex-direction: column;
  }
  
  .btn-row .btn {
    width: 100%;
  }
}

/* ========== 增强样式 ========== */

/* 计数徽章 */
.count-badge {
  display: inline-block;
  background: linear-gradient(135deg, #2d5a87 0%, #4a7ca3 100%);
  color: white;
  font-size: 12px;
  padding: 2px 10px;
  border-radius: 12px;
  margin-left: 8px;
}

/* 增强卡片样式 */
.tool-card.enhanced,
.skill-card.enhanced,
.file-item.enhanced,
.plugin-card.enhanced,
.server-card.enhanced {
  transition: all 0.3s ease;
  border: 1px solid #e9ecef;
}

.tool-card.enhanced:hover,
.skill-card.enhanced:hover,
.plugin-card.enhanced:hover,
.server-card.enhanced:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
  border-color: #2d5a8750;
}

/* 工具卡片头部 */
.tool-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.tool-card.enhanced .tool-name {
  font-size: 15px;
  font-weight: 600;
  color: #1e3a5f;
}

/* 参数列表样式 */
.params-title,
.example-title {
  font-size: 12px;
  font-weight: 600;
  color: #495057;
  margin-bottom: 6px;
}

.params-list {
  background: #f8f9fa;
  border-radius: 6px;
  padding: 8px 12px;
}

.param-item {
  font-size: 12px;
  color: #6c757d;
  padding: 4px 0;
  border-bottom: 1px dashed #e9ecef;
}

.param-item:last-child {
  border-bottom: none;
}

/* 示例代码样式 */
.example-code {
  background: #1e3a5f;
  color: #a7f3d0;
  padding: 10px 14px;
  border-radius: 8px;
  font-size: 12px;
  font-family: 'Monaco', 'Menlo', monospace;
  overflow-x: auto;
  white-space: pre-wrap;
  word-break: break-all;
}

/* 响应框增强 */
.response-header {
  font-size: 13px;
  font-weight: 600;
  color: #495057;
  margin-bottom: 8px;
  padding-bottom: 8px;
  border-bottom: 1px solid #e9ecef;
}

.response-content {
  font-size: 13px;
  color: #212529;
  white-space: pre-wrap;
  word-break: break-word;
  margin: 0;
  font-family: inherit;
}

/* 文件列表增强 */
.file-item.enhanced {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
  border: 1px solid #e9ecef;
  border-radius: 10px;
}

.file-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.file-icon {
  font-size: 20px;
}

.file-actions {
  display: flex;
  gap: 8px;
}

/* 技能卡片增强 */
.skill-card.enhanced {
  padding: 18px;
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  border-radius: 12px;
  border: 1px solid #e9ecef;
}

.skill-card.enhanced .skill-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.skill-card.enhanced .skill-name {
  font-size: 15px;
  font-weight: 600;
  color: #1e3a5f;
}

.skill-meta {
  margin-top: 8px;
  margin-bottom: 8px;
}

.skill-trigger {
  display: inline-block;
  background: #e9ecef;
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 11px;
  color: #495057;
}

.steps-list {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 10px;
}

.step-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 0;
  border-bottom: 1px solid #e9ecef;
}

.step-item:last-child {
  border-bottom: none;
}

.step-order {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  background: linear-gradient(135deg, #2d5a87 0%, #4a7ca3 100%);
  color: white;
  border-radius: 50%;
  font-size: 12px;
  font-weight: 600;
}

.step-name {
  flex: 1;
  font-size: 13px;
  color: #212529;
}

.step-tool {
  font-size: 11px;
  color: #6c757d;
  background: #e9ecef;
  padding: 2px 8px;
  border-radius: 10px;
}

/* 插件卡片增强 */
.plugin-card.enhanced {
  padding: 18px;
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  border-radius: 12px;
  border: 1px solid #e9ecef;
  border-left: 4px solid #6c757d;
}

.plugin-card.enhanced.active {
  border-left-color: #10b981;
}

.plugin-card.enhanced.disabled {
  border-left-color: #ef4444;
}

.plugin-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.plugin-name {
  font-size: 15px;
  font-weight: 600;
  color: #1e3a5f;
}

.plugin-card.enhanced .status-badge {
  float: none;
  font-size: 11px;
  padding: 3px 10px;
  border-radius: 12px;
  font-weight: 500;
}

.status-badge.active {
  background: #d1fae5;
  color: #065f46;
}

.status-badge.disabled {
  background: #fee2e2;
  color: #991b1b;
}

.plugin-meta {
  display: flex;
  gap: 12px;
  margin-bottom: 10px;
  font-size: 12px;
  color: #6c757d;
}

.plugin-desc {
  font-size: 13px;
  color: #6c757d;
  margin-bottom: 12px;
}

.plugin-servers {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 10px;
  margin-bottom: 12px;
}

.servers-label {
  font-size: 12px;
  font-weight: 600;
  color: #495057;
  display: block;
  margin-bottom: 6px;
}

.servers-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.server-tag {
  background: #e9ecef;
  color: #495057;
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 11px;
}

.plugin-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

/* 服务器卡片增强 */
.server-card.enhanced {
  padding: 16px;
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  border-radius: 12px;
  border: 1px solid #e9ecef;
}

/* 小按钮样式 */
.btn-xs {
  padding: 4px 10px;
  font-size: 12px;
}

.btn-outline {
  background: transparent;
  border: 1px solid #2d5a87;
  color: #2d5a87;
}

.btn-outline:hover {
  background: #2d5a87;
  color: white;
}
</style>

