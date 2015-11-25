(ns net.zuobin.vclient
  (:gen-class :main true))

;很简单的一个脚本
;公司有个B2B的项目，由于没有界面，每次想要测试都必须走一大堆步骤
;如果使用Rest Client等工具,有个不可避免的问题是加密解密没法解决。
;所以写了这个工具，打包成jar之后执行一个含有post参数的文件即可。
;同时，好处是里面直接对错误码等做了翻译，不用再去查代码看错误码了。

;Ps:其实很简单的脚本，Python实现早就做了。
;但是为了以后扩展更多的功能，这里用Clojure实现一把。
;毕竟项目是Java写的，很多东西Clojure可以直接使用。

;实现功能
;1. post或者get方式可选
;2. 参数直接定义在一个文档里面，key=value排列即可
;3. 打印输出结果，翻译错误码
;4. 基本校验功能
;5. 自动处理加密解密
;6. 文档里面参数可多个，脚本会依次执行

;暂时只考虑这么多
(require '[clj-http.client :as client])

(defn help
  "help doc"
  []
  (println (format " help : 获取帮助。 \n init 文件名 : 自动创建包含key的文件,value需您填入。 \n 文件 : 执行文件内参数。例如:java -jar **.jar test.txt, PS:命令可以alias别名。\n"))
  )

(defn getFileName
  "获取参数传递的文件名。"
  [& args]
  (first (first args))
  )

(defn checkArgs
  "Check args"
  [& args]
  (let [filename (first args)]
    (condp = (first filename)
      nil (do (println "请输入参数,help参数获取帮助。") false)
      "help" (do (help) false)
      true
    )))

(defn checkFileArgs
  "Check file Args."
  [source]
  )

(defn checkFile
  "Check File"
  [filename]
  (let [source (clojure.java.io/file filename)]
      (if (.exists source)
        (do (println "File exits!") true)
        false
        )
    )
  )

(defn doHttp
  "do main code"
  [arglist]
   (println "doHttp")
  )

(defn -main
  [& args]
  (if (checkArgs args)
      (let [filename (getFileName args)]
          (if (checkFile filename)
              (doHttp args))))
  ;(println (client/get "http://www.baidu.com"))
  )