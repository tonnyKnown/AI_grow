const storage = {
  setToken: function(token) {
    try {
      window.sessionStorage.setItem('oa_token', token);
    } catch (e) {
    }
  },
  getToken: function() {
    try {
      return window.sessionStorage.getItem('oa_token');
    } catch (e) {
      return null;
    }
  },
  removeToken: function() {
    try {
      window.sessionStorage.removeItem('oa_token');
    } catch (e) {
    }
  }
};

export default storage;
