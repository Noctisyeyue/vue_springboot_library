// 跨域配置
module.exports = {
    devServer: {                //设置本地默认端口
        port: 9876,             //前端开发服务器运行在9876端口
        proxy: {                 //设置代理
            '/api': {              //设置拦截器  所有以 /api 开头的请求都会被代理
                target: 'http://localhost:9090',     //代理的目标地址
                changeOrigin: true,              //是否设置同源，输入是的.改变请求头的 origin.伪装成是 9090 端口自己发的请求
                pathRewrite: {                   //路径重写
                    '^/api': ''                     // 去掉 /api 前缀
                }
            }
        }
    }
}
