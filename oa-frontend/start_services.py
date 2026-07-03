import subprocess
import os
import time

def main():
    print("正在启动服务...")
    
    # 1. 启动 Redis
    redis_path = r"E:\redis\redis-server.exe"
    if os.path.exists(redis_path):
        print("启动 Redis...")
        subprocess.Popen(redis_path, creationflags=subprocess.CREATE_NEW_CONSOLE)
        time.sleep(2)
    else:
        print(f"警告: Redis 路径不存在: {redis_path}")
    
    # 2. 启动 Nacos
    nacos_path = r"C:\Users\Administrator\ai-infra\nacos\standalone\nacos-3.2.1\bin"
    nacos_cmd = os.path.join(nacos_path, "startup.cmd")
    if os.path.exists(nacos_cmd):
        print("启动 Nacos...")
        os.chdir(nacos_path)
        subprocess.Popen([nacos_cmd, "-m", "standalone"], creationflags=subprocess.CREATE_NEW_CONSOLE)
        time.sleep(3)
    else:
        print(f"警告: Nacos 路径不存在: {nacos_cmd}")
    
    # 3. 启动 Vue 开发服务器
    vue_path = r"D:\vue\oa-frontend"
    if os.path.exists(vue_path):
        print("启动 Vue 开发服务器...")
        os.chdir(vue_path)
        subprocess.Popen(["cmd", "/k", "npm", "run", "dev"], creationflags=subprocess.CREATE_NEW_CONSOLE)
    else:
        print(f"警告: Vue 项目路径不存在: {vue_path}")
    
    print("\n所有服务已启动！")

if __name__ == "__main__":
    main()
