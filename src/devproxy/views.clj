(ns devproxy.views
  (:require
   [devproxy.conf    :as c]
   [hiccup.form :as hf]
   [hiccup.page :as hp]
   [devproxy.db      :as db]
   [devproxy.keys    :as k]
   [devproxy.mem     :as mem]
   [devproxy.utils   :as u]))

(defn page-header
  [conf]
  [:head
   [:meta {:http-equiv "Cache-Control" :content="no-cache, no-store, must-revalidate"}]
   [:meta {:http-equiv "Pragma" :content "no-cache"}]
   [:meta {:http-equiv "Expires" :content "0"}]
   [:title (:page-title conf)]
   (hp/include-css "/css/bulma.css")
   (hp/include-css "/css/all.css")])

(defn not-found
  []
  (hp/html5
   [:h1 "404 Error!"]
   [:b "Page not found!"]
   [:p [:a {:href ".."} "Return to main page"]]))

(defn missing
  [conf]
  [:section {:class "section"}
   [:div {:class "container content"}
    [:div {:class "box"}
     [:h3 "select standard, year and n<sup><i>(no of devices)</i></sup>"]]]])

(defn not-implemented
  [conf]
  [:section {:class "section"}
   [:div {:class "container content"}
    [:div {:class "box"}
     [:h3 "Not implemented"]]]])

(defn select
  ([conf ename init-vec]
   (select conf ename init-vec nil))
  ([conf ename init-vec row]
   (select conf ename init-vec row nil))
  ([conf ename init-vec row size-attr]
   [:div {:class (str "column " (if size-attr size-attr ""))}
    [:div {:class "field has-addons"}
     [:div {:class "control"}
      [:a {:class "button is-light"} ename]]
    [:div {:class "control"}
     [:div
      (hf/drop-down {:id (if row (u/elem-id conf ename row) ename)
                     :class (str "input is-light " ename)
                     :data-row row}
                    ename init-vec)]]]]))

(defn button
  ([conf ename text]
   (button conf ename text nil))
  ([conf ename text row]
   [:div {:class "column is-1"}
    [:div {:class "field"}
    [:div {:class "control"}
     (hf/submit-button {:id (if row (u/elem-id conf ename row) ename)
                        :class (str "button is-info " ename)
                        :data-row row} text)]]]))

(defn device-link
  [conf row]
  [:div {:class "column is-1"}
   [:div {:class "field"}
   [:div {:class "control"}
    [:a {:href (str "/device/" row)
         :class "is-link "}
     [:i {:class "far fa-arrow-alt-circle-right fa-2x"} ]]]]])

(defn index-link
  [conf]
  [:div {:class "column is-1"}
   [:div {:class "field"}
    [:div {:class "control"}
    [:a {:href "/"
         :class "is-link"}
    [:i {:class "far fa-arrow-alt-circle-left fa-2x"} ]]]]])

(defn device-stdout
  [conf row size-attr]
  [:div {:class (str "column " size-attr)}
   [:div {:class "control"}
    [:textarea {:id (u/elem-id conf "device-stdout" row)
                :class "textarea is-light"
                :data-row row}]]])
(defn reset-button
 [conf]
  [:section {:class "section"}
   [:div {:class "container content"}
    [:div {:class "box"}
     [:div {:class "columns"}
      (button conf "reset" "reset all")]]]])

(defn main-select
  [conf]
  (let [standard   (mem/get-val! (k/standard conf))
        year       (mem/get-val! (k/year conf))
        n          (mem/get-val! (k/n conf))
        maintainer (mem/get-val! (k/maintainer conf))
        gas        (mem/get-val! (k/gas conf))
        mode       (mem/get-val! (k/mode conf))]
  [:section {:class "section"}
   [:div {:class "container content"}
    [:div {:class "box"}
     [:div {:class "columns"}
      (select conf "standard"   (u/fill-kw conf standard :standards))
      (select conf "year"       (u/fill-kw conf year :years))
      (select conf "n"          (u/fill-kw conf n :n))
      (select conf "maintainer" (u/fill-kw conf maintainer :maintainers))
      (select conf "gas"        (u/fill-kw conf gas :gases))
      (select conf "mode"       (u/fill-kw conf mode :modes))]]]]))

(defn device-select
  [conf row]
  (let  [device-vec (db/device-vec conf)
         device     (mem/get-val! (k/device conf row))]
    [:section {:class "section"}
     [:div {:class "container content"}
      [:div {:class "box"}
       [:div {:class "columns"}
        (index-link conf)
        (select conf "device"  (u/fill-vec conf device device-vec) row)]]]]))

(defn default
  [conf row k v]
  [:div {:class "column"}
   [:div {:class "field has-addons"}
    [:div {:class "control"}
     [:a {:class "button is-info"} k]]
    [:div {:class="control"}
     [:input {:class "input is-info defaults"
              :type "text"
              :value v
              :data-key k
              :data-value v
              :data-row row}]]]])

(defn device-defaults
  [conf row defaults-seq]
  [:section {:class "section"}
   [:div {:class "container content"}
    [:div {:class "box"}
     (into [:div {:class "columns"}]
           (map
            (fn [[dk dv]]
              (let [kk (k/defaults conf row (name dk))
                    v (mem/get-val! kk)]
                (default conf row (name dk) v))))
           defaults-seq)]]])

(defn device-tasks
 [conf row tasks]
  [:section {:class "section"}
   [:div {:class "container content"}
    [:div {:class "box"}
     [:div {:class "columns"}
      (select conf "task" (mapv :TaskName tasks) row)
      (button conf "run" "run" row)
      (device-stdout conf row "is-7")]]]])

(defn index-title
  [conf std]
  [:section {:class "hero is-dark"}
   [:div {:class "hero-body"}
      [:div {:class "container"}
       [:h1 {:class "title"} (:main-title conf)]
       [:h2 {:class "subtitle"} (str "standard: " std)]]]])

(defn device-title
  [conf row]
  (let [id (mem/get-val! (k/id conf row))]
    [:section {:class "hero is-dark"}
     [:div {:class "hero-body"}
      [:div {:class "container"}
       [:h1 {:class "title"} (:main-title conf)]
       [:h2 {:class "subtitle"} (str "device setup for id: " id )]]]]))

;;----------------------------------------------------------
;; se3 
;;----------------------------------------------------------
(defn item-se3
  [conf row]
  (let [standard (mem/get-val! (k/standard conf))
        year     (mem/get-val! (k/year conf))
        id       (mem/get-val! (k/id conf row))
        br       (mem/get-val! (k/branch conf row))
        fs       (mem/get-val! (k/fullscale conf row))
        id-vec   (db/cal-ids conf standard year)
        fs-vec   (u/display-fullscale-vec conf)
        br-vec   (get-in conf [:items :se3-branch])]
    (if (and standard year)
      [:div {:class "columns"}
       (button        conf "reset"     "reset"                     row)
       (select        conf "id"        (u/fill-vec conf id id-vec) row "is-3")
       (select        conf "fullscale" (u/fill-vec conf fs fs-vec) row)
       (select        conf "branch"    (u/fill-vec conf br br-vec) row)
       (device-stdout conf row "is-3")
       (device-link   conf row)])))

;;----------------------------------------------------------
;; ce3 
;;----------------------------------------------------------
(defn item-ce3
  [conf row]
  (let [standard (mem/get-val! (k/standard conf))
        year     (mem/get-val! (k/year conf))
        id       (mem/get-val! (k/id conf row))
        fs       (mem/get-val! (k/fullscale conf row))
        fs-vec   (u/display-fullscale-vec conf)
        port     (mem/get-val! (k/port conf row))
        opx      (mem/get-val! (k/opx conf row))
        id-vec   (db/cal-ids conf standard year)
        opx-vec  (get-in conf [:items :ce3-opx])
        port-vec (get-in conf [:items :ce3-port])
        ]
    (if (and standard year)
      [:div {:class "columns"}
       (button        conf "reset"     "reset" row)
       (select        conf "id"        (u/fill-vec conf id   id-vec)   row "is-3")
       (select        conf "port"      (u/fill-vec conf port port-vec) row)
       (select        conf "fullscale" (u/fill-vec conf fs fs-vec)     row)
       (select        conf "opx"       (u/fill-vec conf opx  opx-vec)  row)
       (device-stdout conf row "is-3")
       (device-link   conf row)])))

;;----------------------------------------------------------
;; frs 
;;----------------------------------------------------------
(defn item-frs
  [conf row]
  (let [standard (mem/get-val! (k/standard conf))
        year     (mem/get-val! (k/year conf))
        fs       (mem/get-val! (k/fullscale conf row))
        fs-vec   (u/display-fullscale-vec conf)
        id       (mem/get-val! (k/id conf row))
        id-vec   (db/cal-ids conf standard year)]
    (if (and standard year)
      [:div {:class "columns"}
       (button        conf "reset"     "reset" row)
       (select        conf "id"        (u/fill-vec conf id id-vec) row "is-3")
       (select        conf "fullscale" (u/fill-vec conf fs fs-vec) row)
       (device-stdout conf row "is-3")
       (device-link   conf row)])))

;;----------------------------------------------------------
;; dkm_ppc4 
;;----------------------------------------------------------
(defn item-dkm
  [conf row]
  (let [standard (mem/get-val! (k/standard conf))
        year     (mem/get-val! (k/year conf))
        fs       (mem/get-val! (k/fullscale conf row))
        fs-vec   (u/display-fullscale-vec conf)
        id       (mem/get-val! (k/id conf row))
        id-vec   (db/cal-ids conf standard year)]
    (if (and standard year)
      [:div {:class "columns"}
       (button        conf "reset"     "reset" row)
       (select        conf "id"        (u/fill-vec conf id id-vec) row "is-3")
       (select        conf "fullscale" (u/fill-vec conf fs fs-vec) row)
       (device-stdout conf row "is-3")
       (device-link   conf row)])))

;;----------------------------------------------------------
;; items
;;----------------------------------------------------------
(defn items
  [conf f] 
  (let [standard (mem/get-val! (k/standard conf))
        year     (mem/get-val! (k/year conf))
        n        (mem/get-val! (k/n conf))]
    (if (and standard year n)
      [:section {:class "section"}
       [:div {:class "container content"}
        (into [:div {:class "box"}]
              (map f (range (Integer/parseInt n))))]]
      (missing conf))))

;;----------------------------------------------------------
;; index page
;;----------------------------------------------------------
(defn index
  [conf req]
  (let [s (mem/get-val! (k/standard conf))
        s (if s s "~")]
    (hp/html5
     (page-header conf)
     [:body
      (index-title conf s)
      (main-select conf)
      (condp = (keyword s)
        :FRS5     (items  conf (fn [i] (item-frs conf i)))
        :SE3      (items  conf (fn [i] (item-se3 conf i)))
        :CE3      (items  conf (fn [i] (item-ce3 conf i)))
        :DKM_PPC4 (items  conf (fn [i] (item-dkm conf i)))
        (missing conf))
      (reset-button conf)
      (hp/include-js "/js/jquery-3.5.1.min.js")
      (hp/include-js "/js/ws.js")
      (hp/include-js "/js/main.js")])))


;;----------------------------------------------------------
;; device page
;;----------------------------------------------------------
(defn device
  [conf req row]
  (hp/html5
   (page-header conf)
   [:body
    (device-title conf row)
    (device-select conf row)
    (when-let [device-name (mem/get-val! (k/device conf row))]
      [:div
       (device-defaults conf row (db/device-defaults conf device-name))
       (device-tasks    conf row (db/device-tasks    conf device-name))])
    (hp/include-js "/js/jquery-3.5.1.min.js")
    (hp/include-js "/js/ws.js")
    (hp/include-js "/js/main.js")]))
