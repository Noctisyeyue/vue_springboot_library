// 跨域配置
module.exports = {
    devServer: {                //记住，别写错了devServer//设置本地默认端口  选填
        port: 9876,
        proxy: {                 //设置代理，必须填
            '/api': {              //设置拦截器  拦截器格式   斜杠+拦截器名字，名字可以自己定  拦截器：所有以 /api 开头的请求都会被代理
                target: 'http://localhost:9090',     //代理的目标地址
                changeOrigin: true,              //是否设置同源，输入是的  // 改变请求头的 origin   伪装成是 9090 端口自己发的请求
                pathRewrite: {                   //路径重写
                    '^/api': ''                     // 去掉 /api 前缀
                }
            }
        }
    }
}
