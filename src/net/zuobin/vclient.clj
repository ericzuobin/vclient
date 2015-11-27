(ns net.zuobin.vclient
  (:gen-class :main true)
  (:use [clojure.java.io]))

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
;3. 打印输出结果，翻译错误码(暂不支持)
;4. 基本校验功能
;5. 自动处理加密解密
;6. 文档里面参数可多个，脚本会依次执行(暂不支持)

;暂时只考虑这么多
(require '[clj-http.client :as client])

(def fileHeader ["url" "method" "merchant" "key" "command"])

(def fileBody ["lotteryType" "PlayType" "phaceNo" "content" "multiple" "amount"])

(defn getMD5
  "get MD5"
  [key]
  (apply str
     (map (partial format "%02x")
          (.digest (doto (java.security.MessageDigest/getInstance "MD5")
                     .reset (.update (.getBytes key)))))))

(defn help
  "help doc"
  []
  (println (format " help : 获取帮助。 \n init 文件名 : 自动创建包含key的文件,value需您填入。 \n 文件 : 执行文件内参数。例如:java -jar **.jar test.txt, PS:命令可以alias别名。\n")))

(defn checkFile
  "Check File is exits, return File or nil"
  [filename]
  (if filename (let [source (clojure.java.io/file filename)]
      (if (.exists source) true false)) true))

(defn init
  "Init File,Return a new File or nil"
  [filename]
   (if (checkFile filename) (println "输入的文件名已存在或为空,请检查。")
     (with-open [wtr (writer filename)]
       (.write wtr (println-str "#Header,以下信息需要找开发或者从测试环境中取,其中method参数为get或者post;"))
       (doseq [key fileHeader]
         (.write wtr (println-str key "=" ";")))
       (.write wtr (println-str "#Body,以下信息为投注信息,以~~~分隔每注投注;"))
       (doseq [key fileBody]
         (.write wtr (println-str key "=" ";"))))))

(defn checkArgs
  "Check args,return nill or File"
  [& args]
  (let [fArg (first args)]
    (condp = (first fArg)
      nil (println "请输入参数,help参数获取帮助。")
      "help" (help)
      "init" (init (second fArg))
      true)))

(defn getFileArgs
  "Get the Get/Post Data."
  [filename]
    (with-open [rdr (reader filename)]
      (apply conj {} (for [fileLine (line-seq rdr)]
          (let [line (.trim fileLine)]
            (if (not (.startsWith line "#"))
              {(.trim (.substring line 0 (- (.indexOf line "=") 1)))
               (.trim(.substring line (+ (.indexOf line "=") 1) (.indexOf line ";")))}
                {}))))))
(defn getbody
  "get query Body!"
  [queryMap]
  (str "<message><merchant>" (queryMap "merchant")  "</merchant><realname>weizhi</realname><idcard>234102193410020318</idcard><mobile>18610105738</mobile>"
       "<orderlist><order><lotterytype>"  (queryMap "lotteryType")  "</lotterytype>"
       "<phaseno>" (queryMap "phaceNo") "</phaseno>"
       "<orderid>" (.format (java.text.SimpleDateFormat. "yyMMddHHmmssSSS") (java.util.Date.)) (format "%4d" (rand-int 9999)) "</orderid>"
       "<playtype>" (queryMap "PlayType") "</playtype>"
       "<content>" (queryMap "content") "</content><addition>0</addition>"
       "<multiple>" (queryMap "multiple") "</multiple>"
       "<amount>" (queryMap "amount") "</amount></order></orderlist></message>"))

(defn getHeader
  "get header!"
  [queryMap]
  (let [timeStamp (.format (java.text.SimpleDateFormat. "yyMMddHHmmss") (java.util.Date.))
        merchant (queryMap "merchant")
        command (queryMap "command")
        requestid (str (queryMap "merchant") (.format (java.text.SimpleDateFormat. "yyMMddHHmmssSSS") (java.util.Date.)) (format "%4d" (rand-int 9999)))
        body (getbody queryMap)
        md5 (getMD5 (str (queryMap "command") timeStamp (queryMap "merchant")(queryMap "key") ))
        ]
      (let [query (str "<?xml version=\"1.0\" encoding=\"UTF-8\"?><content><head><version>1</version>"
                 "<merchant>" merchant "</merchant>"
                 "<command>" command "</command>"
                 "<encrypttype>0</encrypttype><compresstype>0</compresstype><custom></custom>"
                 "<timestamp>" timeStamp "</timestamp>"
                 "<requestid>" requestid "</requestid>"
                 "</head><body>" body "</body>"
                 "<signature>" md5 "</signature></content>")]
        (println (queryMap "method") ":" query) query)))


(defn doHttp
  "do main code"
  [filename]
    (let [queryMap (getFileArgs filename)]
      (if (= (queryMap "method") "get")
         (println (client/get (queryMap "url") {:body (getHeader queryMap)})))))

(defn -main
  [& args]
  (if (checkArgs args)
      (let [a (first args)]
          (doHttp a)))
  )