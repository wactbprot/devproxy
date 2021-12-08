(ns devproxy.views
  ^{:author "Thomas Bock <thomas.bock@ptb.de>"
    :doc "Provides the gui (html-pages) for interaction."}
  (:require [devproxy.conf :as c]
            [hiccup.form :as hf]
            [hiccup.page :as hp]
            [devproxy.db :as db]
            [devproxy.keys :as k]
            [devproxy.mem :as mem]
            [devproxy.utils :as u]))

(defn page-header
  ([conf]
   (page-header conf (:page-title conf)))
  ([conf title]
   [:head
    [:meta {:http-equiv "Cache-Control" :content="no-cache, no-store, must-revalidate"}]
    [:meta {:http-equiv "Pragma" :content "no-cache"}]
    [:meta {:http-equiv "Expires" :content "0"}]
    [:title title]
    (hp/include-css "/css/bulma.css")]))

(defn not-found []
  (hp/html5
   [:h1 "404 Error!"]
   [:b "Page not found!"]
   [:p [:a {:href ".."} "Return to main page"]]))

(defn missing [conf]
  [:section.section
   [:div.container.content
    [:div.box
     [:h3 "select standard, year and n<sup><i>(no of devices)</i></sup>"]]]])

(defn not-implemented [conf]
  [:section.section
   [:div.container.content
    [:div.box
     [:h3 "Not implemented"]]]])

(defn select
  ([conf ename init-vec]
   (select conf ename init-vec nil))
  ([conf ename init-vec row]
   (select conf ename init-vec row nil))
  ([conf ename init-vec row size-attr]
   [:div.column {:class (when size-attr size-attr)}
    [:div.field.has-addons
     [:div.control
      [:a.button.is-light ename]]
     [:div.control
      [:div
       (hf/drop-down {:id (if row (u/elem-id conf ename row) ename)
                      :class (str "input is-light " ename)
                      :data-row row}
                     ename init-vec)]]]]))

(defn button
  ([conf ename text]
   (button conf ename text nil))
  ([conf ename text row]
   [:div.column
    (hf/submit-button {:id (if row (u/elem-id conf ename row) ename)
                       :class (str "button is-info " ename)
                       :data-row row} text)]))

(defn link [conf ref s]
  [:div.column
   [:div.tags.has-addons 
    [:span.tag.is-medium "&#10143;"]
    [:a.tag.is-info.is-medium {:href ref} s]]])

(defn device-link [conf row] (link conf (str "/device/" row) " select readout device"))

(defn index-link [conf]  (link conf "/" " home"))

(defn device-stdout [conf row size-attr]
  [:div.column {:class size-attr}
   [:div.control
    [:textarea.textarea.is-light 
     {:placeholder "device request result"
      :disabled ""
      :id (u/elem-id conf "device-stdout" row)
      :data-row row}]]])

(defn reset-button [conf]
  [:section.section
   [:div.container.content
    [:div.box
     [:div.columns (button conf "reset" "reset all")]]]])

(defn main-select [conf]
  (let [standard   (mem/get-val! (k/standard conf))
        year       (mem/get-val! (k/year conf))
        n          (mem/get-val! (k/n conf))
        maintainer (mem/get-val! (k/maintainer conf))
        gas        (mem/get-val! (k/gas conf))
        mode       (mem/get-val! (k/mode conf))]
  [:section.section
   [:div.container.content
    [:div.box
     [:div.columns
      (select conf "standard"   (u/fill-kw conf standard :standards))
      (select conf "year"       (u/fill-kw conf year :years))
      (select conf "n"          (u/fill-kw conf n :n))]
     [:div.columns
      (select conf "maintainer" (u/fill-kw conf maintainer :maintainers))
      (select conf "mode"       (u/fill-kw conf mode :modes))
      (select conf "gas"        (u/fill-kw conf gas :gases))]]]]))

(defn device-select [conf row]
  (let  [device-vec (db/device-vec conf)
         device     (mem/get-val! (k/device conf row))]
    [:section.section
     [:div.container.content
      [:div.box
       [:div.columns
        (select conf "device"  (u/fill-vec conf device device-vec) row)
        (index-link conf)]]]]))

(defn default [conf row k v]
  [:div.column
   [:div.field.has-addons
    [:div.control
     [:a.button.is-info k]]
    [:div.control
     [:input.input.is-info.defaults
      {:type "text"
       :value v
       :data-key k
       :data-value v
       :data-row row}]]]])

(defn device-defaults [conf row defaults-seq]
  (when (seq defaults-seq)
    [:section.section
     [:div.container.content
      [:div.box
       (into [:div.columns]
             (map
              (fn [[dk dv]]
                (let [kk (k/defaults conf row (name dk))
                      v (mem/get-val! kk)]
                  (default conf row (name dk) v))))
             defaults-seq)]]]))

;;----------------------------------------------------------
;; manual input tasks
;;----------------------------------------------------------
(defn input [conf row taskname k v]
  [:div.column
    [:div.field-body
     [:div.field
      [:p.control
       [:label.label k
        [:input.input.is-info.input-value
         {:data-type (condp = k
                       :Value "float"
                       :Type "string"
                       :Unit "string"
                       :SdValue "float"
                       :N "integer"
                       "string")
          :data-value v
          :value v
          :data-taskname taskname
          :data-row row
          :data-key (name k)}]]]]]])

(defn ready-button [conf row taskname]
  [:button.button.is-info.ready-button
   {:data-row row
    :data-taskname taskname
    :data-key  "Ready" } "ok"])

(defn device-man-tasks [conf row tasks]
  (when (seq tasks)
    (into [:section.section]
          (map (fn [{value :Value taskname :TaskName}]
                 (let [{u :Unit t :Type v :Value s :SdValue n :N} value]
                   [:section.section
                    [:h3.subtitle taskname]
                    [:div.columns
                     (input conf row taskname :Type t)
                     (input conf row taskname :Value v )
                     (input conf row taskname :Unit u )
                     (when s (input conf row taskname :SdValue s))
                     (when n (input conf row taskname :N n))]
                    (ready-button conf row taskname)]))
               tasks))))

;;----------------------------------------------------------
;; devhub tasks
;;----------------------------------------------------------
(defn device-dev-tasks [conf row tasks]
  (when (seq tasks)
    [:div.columns
     (select conf "task" (mapv :TaskName tasks) row)
     (button conf "run" "run" row)
     (device-stdout conf row "is-7")]))

;;----------------------------------------------------------
;; device tasks
;;----------------------------------------------------------
(defn device-tasks [conf row tasks]
  [:div.container.content
   [:div.box
    (device-dev-tasks conf row (filterv #(not= "manualInput" (:Action %)) tasks))
    (device-man-tasks conf row (filterv #(= "manualInput" (:Action %)) tasks))]])

;;----------------------------------------------------------
;; title
;;----------------------------------------------------------
(defn index-title [conf std]
  [:section.hero.is-info
   [:div.hero-body
      [:div.container
       [:h1.title (:main-title conf)]
       [:h2.subtitle std]]]])

(defn device-title [conf row]
  (let [id (mem/get-val! (k/id conf row))]
    [:section.hero.is-info
     [:div.hero-body
      [:div.container
       [:h1.title (:main-title conf)]
       [:h2.subtitle (when id "Certificate: " (u/id->cert-issue id))]]]]))

;;----------------------------------------------------------
;; se3 
;;----------------------------------------------------------
(defn item-se3 [conf row]
  (let [standard (mem/get-val! (k/standard conf))
        year     (mem/get-val! (k/year conf))
        id       (mem/get-val! (k/id conf row))
        br       (mem/get-val! (k/branch conf row))
        fs       (mem/get-val! (k/fullscale conf row))
        id-vec   (db/cal-ids conf standard year)
        fs-vec   (u/display-fullscale-vec conf)
        br-vec   (get-in conf [:items :se3-branch])]
    (if (and standard year)
      [:div.box
       [:h3.title "Device № " (inc row)]
       [:div.columns
        (select conf "id"        (u/fill-vec conf id id-vec) row "is-3")
        (select conf "fullscale" (u/fill-vec conf fs fs-vec) row)
        (select conf "branch"    (u/fill-vec conf br br-vec) row)
        (device-stdout conf row "is-4")]
       [:div.columns
        (button conf "reset" "reset" row)
        (device-link conf row)]])))

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
      [::div.box
       [:h3.title "Device № " row]
       [:div.columns
        (select        conf "id"        (u/fill-vec conf id   id-vec)   row "is-3")
        (select        conf "port"      (u/fill-vec conf port port-vec) row)
        (select        conf "fullscale" (u/fill-vec conf fs fs-vec)     row)
        (select        conf "opx"       (u/fill-vec conf opx  opx-vec)  row)
        (device-stdout conf row "is-4")]
       [:div.columns
        (button conf "reset" "reset" row)
        (device-link   conf row)]])))

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
      [:div.box
       [:h3.title "Device № " row]
       [:div.columns
        (select        conf "id"        (u/fill-vec conf id id-vec) row "is-3")
        (select        conf "fullscale" (u/fill-vec conf fs fs-vec) row)
        (device-stdout conf row "is-4")]
       [:div.columns
        (button conf "reset" "reset" row)
        (device-link   conf row)]])))

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
      [::div.box
       [:h3.title "Device № " row]
       [:div.columns
        (select        conf "id"        (u/fill-vec conf id id-vec) row "is-3")
        (select        conf "fullscale" (u/fill-vec conf fs fs-vec) row)
        (device-stdout conf row "is-3")]
       [:div.columns
        (button conf "reset" "reset" row)
        (device-link   conf row)]])))

;;----------------------------------------------------------
;; items
;;----------------------------------------------------------
(defn items [conf f] 
  (let [standard (mem/get-val! (k/standard conf))
        year     (mem/get-val! (k/year conf))
        n        (mem/get-val! (k/n conf))]
    (if (and standard year n)
      (into [:div.container.content]
            (map f (range (Integer/parseInt n))))
      (missing conf))))

;;----------------------------------------------------------
;; index page
;;----------------------------------------------------------
(defn index [conf req]
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
      (hp/include-js "/js/jquery.js")
      (hp/include-js "/js/ws.js")
      (hp/include-js "/js/main.js")])))


;;----------------------------------------------------------
;; device page
;;----------------------------------------------------------
(defn device [conf req row]
  (let [id (mem/get-val! (k/id conf row))
        title (if id (u/id->cert-issue id) "~")]
    (hp/html5
     (page-header conf title)
     [:body
      (device-title conf row)
      (device-select conf row)
      (when-let [device-name (mem/get-val! (k/device conf row))]
        [:div
         (device-defaults conf row (db/device-defaults conf device-name))
         (device-tasks    conf row (db/device-tasks    conf device-name))])
      (hp/include-js "/js/jquery.js")
      (hp/include-js "/js/ws.js")
      (hp/include-js "/js/main.js")])))
