import request from '../utils/request'

export const chatApi = {
    sendMessage: (message) => {
        return request({
            url: '/business//chat/send',
            method: 'post',
            data: {
                message
            }
        })
    },
    
    sendKnowledgeMessage: (data) => {
        return request({
            url: '/business/chat/chatRobot',
            method: 'post',
            data: data
        })
    },
    
    getChatHistory: (sessionId, limit) => {
        return request({
            url: `/business/chat/history/${sessionId}`,
            method: 'get',
            params: {
                limit: limit || 10
            }
        })
    },
    
    getUserSessions: (userId, botType) => {
        return request({
            url: `/business/chat/sessions/${userId}`,
            method: 'get',
            params: {
                bot_type: botType
            }
        })
    },
    
    clearSession: (sessionId) => {
        return request({
            url: '/business/chat/session/clear',
            method: 'post',
            data: {
                session_id: sessionId
            }
        })
    },
    
    deleteSession: (sessionId) => {
        return request({
            url: `/business/chat/session/${sessionId}`,
            method: 'delete'
        })
    },
    
    getHistory: (sessionId) => {
        return request({
            url: '/business//chat/history',
            method: 'get',
            params: {
                sessionId
            }
        })
    },
    
    createSession: () => {
        return request({
            url: '/business//chat/session',
            method: 'post'
        })
    }
}