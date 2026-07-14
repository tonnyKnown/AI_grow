import request from '@/utils/request'

export const javachainApi = {
  // ReAct 推理相关
  startReact: (question) => {
    return request({
      url: '/javachain/agent/react',
      method: 'post',
      data: { question },
      timeout: 0
    })
  },
  getJobStatus: (jobId) => {
    return request({
      url: `/javachain/agent/react/status/${jobId}`,
      method: 'get',
      timeout: 0
    })
  },
  confirmJob: (jobId, confirm) => {
    return request({
      url: '/javachain/agent/react/confirm',
      method: 'post',
      data: { jobId, confirm },
      timeout: 0
    })
  },
  
  // 简单对话
  createSession: () => {
    return request({
      url: '/javachain/chat/session/create',
      method: 'post'
    })
  },
  clearSession: (sessionId) => {
    return request({
      url: `/javachain/chat/session/${sessionId}/clear`,
      method: 'post'
    })
  },
  sendMessage: (sessionId, message) => {
    return request({
      url: '/javachain/chat/simple/with-history',
      method: 'post',
      timeout: 30000,
      data: { sessionId, message }
    })
  },
  
  // 智能助手
  sendAutoMessage: (question) => {
    return request({
      url: '/javachain/agent/react',
      method: 'post',
      data: { question },
      timeout: 0
    })
  },
  
  // RAG 问答
  queryKnowledge: (question) => {
    return request({
      url: '/javachain/chat/rag',
      method: 'post',
      data: { question }
    })
  },
  addDocument: (title, content) => {
    return request({
      url: '/javachain/chat/rag/load',
      method: 'post',
      data: { title, text: content }
    })
  },
  clearKnowledgeBase: () => {
    return request({
      url: '/javachain/chat/rag/clear',
      method: 'post'
    })
  },
  getRagStats: () => {
    return request({
      url: '/javachain/chat/rag/stats',
      method: 'get'
    })
  },
  
  // 文件管理
  getFilesList: () => {
    return request({
      url: '/javachain/chat/files/list',
      method: 'get'
    })
  },
  vectorizeFiles: () => {
    return request({
      url: '/javachain/chat/files/vectorize',
      method: 'get'
    })
  },
  vectorizeSingleFile: (filePath) => {
    return request({
      url: '/javachain/chat/files/vectorize/single',
      method: 'post',
      data: { filePath }
    })
  },
  
  // MCP 工具
  getTools: () => {
    return request({
      url: '/javachain/chat/mcp/tools',
      method: 'get'
    })
  },
  executeTool: (toolName, args) => {
    return request({
      url: '/javachain/chat/mcp/execute',
      method: 'post',
      data: { toolName, arguments: args }
    })
  },
  
  // Skills
  getSkills: () => {
    return request({
      url: '/javachain/skills/list',
      method: 'get'
    })
  },
  reloadSkills: () => {
    return request({
      url: '/javachain/skills/reload',
      method: 'get'
    })
  },
  executeSkill: (skillId, params) => {
    return request({
      url: `/javachain/skills/execute/${skillId}`,
      method: 'post',
      data: params || {}
    })
  },
  executeSkillAuto: (input, params) => {
    return request({
      url: '/javachain/skills/auto',
      method: 'post',
      data: { input, params: params || {} }
    })
  },
  
  // MCP 插件管理
  getServers: () => {
    return request({
      url: '/javachain/mcp/servers',
      method: 'get'
    })
  },
  unregisterServer: (serverName) => {
    return request({
      url: `/javachain/mcp/servers/${serverName}`,
      method: 'delete'
    })
  },
  loadPlugin: (jarPath) => {
    return request({
      url: '/javachain/mcp/plugins/load',
      method: 'post',
      data: { jarPath }
    })
  },
  
  // 插件治理
  getGovernancePlugins: () => {
    return request({
      url: '/javachain/plugin-governance/list?includeDisabled=true',
      method: 'get'
    })
  },
  enablePlugin: (pluginId) => {
    return request({
      url: `/javachain/plugin-governance/enable/${pluginId}`,
      method: 'post'
    })
  },
  disablePlugin: (pluginId) => {
    return request({
      url: `/javachain/plugin-governance/disable/${pluginId}`,
      method: 'post'
    })
  },
  checkPluginCompatibility: (pluginId, version) => {
    return request({
      url: `/javachain/plugin-governance/check/${pluginId}/${version}`,
      method: 'get'
    })
  },
  compareVersions: (v1, v2) => {
    return request({
      url: '/javachain/plugin-governance/compare',
      method: 'post',
      data: { v1, v2 }
    })
  },
  
  // 执行 MCP 工具
  executeMcpTool: (toolName, args) => {
    return request({
      url: `/javachain/mcp/tools/${toolName}/execute`,
      method: 'post',
      data: args || {}
    })
  }
}
