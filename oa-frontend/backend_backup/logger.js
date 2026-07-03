const isDev = import.meta.env.DEV

const logger = {
  log: function(...args) {
    if (isDev) {
      console.log.apply(console, args)
    }
  },
  warn: function(...args) {
    if (isDev) {
      console.warn.apply(console, args)
    }
  },
  error: function(...args) {
    if (isDev) {
      console.error.apply(console, args)
    }
  },
  debug: function(...args) {
    if (isDev) {
      console.debug.apply(console, args)
    }
  }
}

export default logger
