<template>
  <el-dialog v-model="visible" title="物流轨迹" width="750px" @closed="handleClose">
    <div v-if="expressData" class="tracking-container">
      <!-- 物流概览 -->
      <div class="tracking-header">
        <div class="tracking-company">{{ expressData.expressCompany }}</div>
        <div class="tracking-no">运单号：{{ expressData.expressNo }}</div>
        <el-tag :type="getStatusType(currentStatus)" size="small" style="margin-top: 8px">
          {{ getStatusText(currentStatus) }}
        </el-tag>
        <el-button type="warning" size="small" class="refresh-btn" :loading="refreshing" @click="handleRefresh">
          <el-icon><Refresh /></el-icon> 实时刷新
        </el-button>
      </div>

      <el-divider />

      <!-- 地图区域 -->
      <div class="map-container" ref="mapContainer"></div>

      <el-divider />

      <!-- 轨迹时间线 -->
      <div v-if="trackingNodes.length > 0">
        <h4 class="timeline-title">物流轨迹详情</h4>
        <el-timeline>
          <el-timeline-item
            v-for="(node, index) in trackingNodes"
            :key="index"
            :timestamp="node.time"
            :type="index === 0 ? 'primary' : 'info'"
            :hollow="index !== 0"
          >
            <p class="tracking-desc">{{ node.desc }}</p>
            <p v-if="node.location" class="tracking-location">{{ node.location }}</p>
          </el-timeline-item>
        </el-timeline>
      </div>
      <div v-else>
        <el-empty description="暂无物流轨迹" />
      </div>
    </div>
    <template #footer>
      <el-button @click="visible = false">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch, nextTick, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { queryRealTimeExpress } from '@/api/express'

const props = defineProps({
  modelValue: Boolean,
  expressData: Object
})

const emit = defineEmits(['update:modelValue'])

const visible = ref(props.modelValue)
const refreshing = ref(false)
const mapContainer = ref(null)
let mapInstance = null
let markerGroup = null
const currentStatus = ref(0)
const trackingNodes = ref([])

watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val && props.expressData) {
    nextTick(() => {
      initData()
      initMap()
    })
  }
})

watch(visible, (val) => {
  emit('update:modelValue', val)
  if (!val) {
    handleClose()
  }
})

const getStatusType = (status) => {
  const types = { 0: 'warning', 1: 'primary', 2: '', 3: 'success', 4: 'danger' }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = { 0: '已揽收', 1: '运输中', 2: '派送中', 3: '已签收', 4: '异常' }
  return texts[status] || '未知'
}

const initData = () => {
  if (!props.expressData) return
  currentStatus.value = props.expressData.status || 0
  try {
    trackingNodes.value = props.expressData.trackingNodes
      ? JSON.parse(props.expressData.trackingNodes)
      : []
  } catch {
    trackingNodes.value = []
  }
}

const initMap = () => {
  if (!mapContainer.value) return

  // 检查 Leaflet 是否加载完成
  if (!window.L) {
    console.warn('Leaflet 未加载，尝试重新加载...')
    setTimeout(() => initMap(), 500)
    return
  }

  // 清理旧地图
  if (mapInstance) {
    mapInstance.remove()
    mapInstance = null
  }

  const L = window.L
  const nodesWithCoords = trackingNodes.value.filter(n => n.lat && n.lng)

  // 默认中心（中国中部）
  let center = [35.86, 104.19]
  let zoom = 3
  let bounds = null

  if (nodesWithCoords.length > 0) {
    const latest = nodesWithCoords[nodesWithCoords.length - 1]
    center = [latest.lat, latest.lng]
    zoom = 13
    bounds = L.latLngBounds(nodesWithCoords.map(n => [n.lat, n.lng]))
  }

  mapInstance = L.map(mapContainer.value, {
    center: center,
    zoom: zoom,
    zoomControl: true
  })

  // 高德地图瓦片（国内可访问，无需 API Key）
  L.tileLayer('https://webrd01.is.autonavi.com/appmaptile?lang=zh_cn&size=1&scale=1&style=8&x={x}&y={y}&z={z}', {
    attribution: '&copy; 高德地图 AutoNavi',
    maxZoom: 18
  }).addTo(mapInstance)

  // 添加轨迹线
  if (nodesWithCoords.length >= 2) {
    const latlngs = nodesWithCoords.map(n => [n.lat, n.lng])
    L.polyline(latlngs, {
      color: '#409EFF',
      weight: 3,
      opacity: 0.7
    }).addTo(mapInstance)
  }

  // 添加坐标点标记
  markerGroup = L.layerGroup().addTo(mapInstance)
  nodesWithCoords.forEach((node, index) => {
    const icon = index === 0
      ? L.divIcon({ className: 'custom-marker', html: '<div style="background:#67C23A;color:#fff;width:28px;height:28px;border-radius:50%;display:flex;align-items:center;justify-content:center;font-size:12px;font-weight:bold;border:2px solid #fff;box-shadow:0 2px 6px rgba(0,0,0,0.3)">' + (nodesWithCoords.length - index) + '</div>', iconSize: [28, 28], iconAnchor: [14, 14] })
      : L.divIcon({ className: 'custom-marker', html: '<div style="background:#409EFF;color:#fff;width:24px;height:24px;border-radius:50%;display:flex;align-items:center;justify-content:center;font-size:11px;font-weight:bold;border:2px solid #fff;box-shadow:0 2px 4px rgba(0,0,0,0.2)">' + (nodesWithCoords.length - index) + '</div>', iconSize: [24, 24], iconAnchor: [12, 12] })

    const marker = L.marker([node.lat, node.lng], { icon })
    marker.bindPopup(`
      <div style="font-size:13px;line-height:1.6">
        <strong>${node.desc}</strong><br/>
        <span style="color:#666">${node.time}</span><br/>
        <span style="color:#999">${node.location || ''}</span>
      </div>
    `)
    markerGroup.addLayer(marker)
  })

  if (bounds && nodesWithCoords.length > 1) {
    mapInstance.fitBounds(bounds, { padding: [30, 30] })
  }

  // 延迟刷新地图（解决弹窗动画导致的渲染问题）
  setTimeout(() => { if (mapInstance) mapInstance.invalidateSize() }, 100)
  setTimeout(() => { if (mapInstance) mapInstance.invalidateSize() }, 500)
}

// 实时刷新
const handleRefresh = async () => {
  if (!props.expressData?.id) {
    ElMessage.warning('无法获取物流信息')
    return
  }
  refreshing.value = true
  try {
    const res = await queryRealTimeExpress(props.expressData.id)
    if (res.data && res.data.trackingNodes) {
      currentStatus.value = res.data.status
      trackingNodes.value = res.data.trackingNodes
      nextTick(() => {
        initMap()
      })
      ElMessage.success('已更新最新物流轨迹')
    } else {
      ElMessage.info('暂无新的物流轨迹')
    }
  } catch (error) {
    ElMessage.error('实时查询失败')
  } finally {
    refreshing.value = false
  }
}

const handleClose = () => {
  if (mapInstance) {
    mapInstance.remove()
    mapInstance = null
  }
}

onBeforeUnmount(() => {
  if (mapInstance) {
    mapInstance.remove()
    mapInstance = null
  }
})
</script>

<style scoped>
.tracking-container {
  padding: 0 8px;
}

.tracking-header {
  text-align: center;
  margin-bottom: 8px;
  position: relative;
}

.tracking-company {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.tracking-no {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.refresh-btn {
  position: absolute;
  right: 0;
  top: 0;
}

.map-container {
  width: 100%;
  height: 320px;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  z-index: 1;
}

.timeline-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 16px 0;
}

.tracking-desc {
  font-size: 14px;
  color: #303133;
}

.tracking-location {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}
</style>
