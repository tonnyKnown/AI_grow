import mitt from 'mitt'

const emitter = mitt()

export const categoryEvents = {
  CATEGORY_CHANGED: 'category-changed'
}

export function emitCategoryChanged() {
  emitter.emit(categoryEvents.CATEGORY_CHANGED)
}

export function onCategoryChanged(handler) {
  emitter.on(categoryEvents.CATEGORY_CHANGED, handler)
}

export function offCategoryChanged(handler) {
  emitter.off(categoryEvents.CATEGORY_CHANGED, handler)
}

export default emitter
