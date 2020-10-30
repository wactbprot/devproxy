(ns aoc.views
  (:require
   [aoc.conf    :as c]
   [hiccup.form :as hf]
   [hiccup.page :as hp]
   [aoc.db    :as db]
   [aoc.keys    :as k]
   [aoc.mem     :as m]
   [aoc.utils   :as u]

   ))

(defn page-header
  [conf]
  [:head
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
     [:h3 "select an standard and year"]]]])

(defn not-implemented
  [conf]
  [:section {:class "section"}
   [:div {:class "container content"}
    [:div {:class "box"}
     [:h3 "Standard is not implemented"]]]])

(defn select
  ([conf ename init-vec]
   (select conf ename init-vec nil))
  ([conf ename init-vec row]
   [:div {:class "column"}
    [:div {:class "field"}
    [:div {:class "control"}
     [:div
      (hf/drop-down {:id (if row (u/elem-id conf ename row) ename)
                     :class (if row
                              (str "input is-info " ename)
                              (str "input is-info " ename))
                     :data-row row}
                    ename init-vec)]]]]))



(defn button
  ([conf ename text]
   (button conf ename text nil))
  ([conf ename text row]
   [:div {:class "column is-1"}
    [:div {:class "control"}
     (hf/submit-button {:id (if row (u/elem-id conf ename row) ename)
                        :class (str "button " (if row "is-info " "is-info ") ename)
                        :data-row row} text)]]))

(defn device-link
  [conf row]
  [:div {:class "column is-1"}
   [:div {:class "control"}
    [:a {:href (str "/device/" row)
         :class "button is-link"} "setup"]
    ;;[:i {:class "fas fa-external-link-alt fa-2x"} ]
    ]])

(defn device-out
  [conf row]
  [:div {:class "column is-3"}
  [:div {:class "control"}
   [:textarea {:id (u/elem-id conf "device-out" row)
               :class "textarea is-info"
               :data-row row}]]])

(defn main-select
  [conf]
  (let [standard   (m/get-val! (k/standard conf))
        year       (m/get-val! (k/year conf))
        maintainer (m/get-val! (k/maintainer conf))
        gas        (m/get-val! (k/gas conf))
        mode       (m/get-val! (k/mode conf))]
  [:section {:class "section"}
   [:div {:class "container content"}
    [:div {:class "box"}
     [:div {:class "columns"}
      (button conf "reset" "reset")
      (select conf "standard"   (u/fill-kw conf standard :standards))
      (select conf "year"       (u/fill-kw conf year :years))
      (select conf "maintainer" (u/fill-kw conf maintainer :maintainers))
      (select conf "gas"        (u/fill-kw conf gas :gases))
      (select conf "mode"       (u/fill-kw conf mode :modes))]]]]))


(defn device-select
  [conf row]
  (let  [device-vec (db/device-vec conf)
         device     (m/get-val! (k/device conf row))]
    [:section {:class "section"}
     [:div {:class "container content"}
      [:div {:class "box"}
       [:div {:class "columns"}
        (select conf "device"  (u/fill-vec conf device device-vec) row)]]]]))


(defn device-defaults!
  "Stores the defaults to mem."
  [conf row]
  (let [dev      (m/get-val! (k/device conf row))
        defaults (db/device-defaults conf dev)]
    [:section {:class "section"}
     [:div {:class "container content"}
      [:div {:class "box"}
       (into [:div {:class "columns"}]
             (map
              (fn [[k v]]
                (let [k (name k)
                      m-val (m/get-val! (k/defaults conf row k))]
                  (when-not m-val
                    (m/set-val! (k/defaults conf row k) v))
                  [:div {:class "column"}
                   [:div {:class "field has-addons"}
                    [:div {:class "control"}
                     [:a {:class "button is-info"} k]]
                    [:div {:class="control"}
                     [:input {:class "input defaults"
                              :type "text"
                              :value (if m-val m-val v)
                              :data-key k
                              :data-value (if m-val m-val v)
                              :data-row row}]]]]))
                (seq defaults)))]]]))

(defn index-title
  [conf std]
  [:section {:class "hero is-info"}
   [:div {:class "hero-body"}
      [:div {:class "container"}
       [:h1 {:class "title"} (:main-title conf)]
       [:h2 {:class "subtitle"} (str "standard: " std)]]]])


(defn device-title
  [conf row]
  (let [id   (m/get-val! (k/id conf row))]
  [:section {:class "hero is-info"}
   [:div {:class "hero-body"}
      [:div {:class "container"}
       [:h1 {:class "title"} (:main-title conf)]
       [:h2 {:class "subtitle"} (str "device setup for id: " id )]]]]))

(defn item-se3
  [conf row]
  (let [standard (m/get-val! (k/standard conf))
        year     (m/get-val! (k/year conf))
        id       (m/get-val! (k/id conf row))
        br       (m/get-val! (k/branch conf row))
        fs       (m/get-val! (k/fullscale conf row))
        id-vec   (db/cal-ids conf standard year)
        fs-vec   (get-in conf [:se3 :items :fullscale])
        br-vec   (get-in conf [:se3 :items :branch])]
    (if (and standard year)
      [:div {:class "columns"}
       (button conf "reset" "reset" row)
       (select conf "id"        (u/fill-vec conf id id-vec) row)
       (select conf "fullscale" (u/fill-vec conf fs fs-vec) row)
       (select conf "branch"    (u/fill-vec conf br br-vec) row)

       (device-link conf row)
       (device-out conf row)
       ])))

(defn items-se3
  "BTW:
  ```clojure
  (into [:div ] (range 9))
  ;; =>
  ;; [:div 0 1 2 3 4 5 6 7 8]
  ```
  "
  [conf]
  (let [standard (m/get-val! (k/standard conf))
        year     (m/get-val! (k/year conf))
        no       (get-in conf [:se3 :no-of-devs])]
    (if (and standard year)
      [:section {:class "section"}
       [:div {:class "container content"}
        (into
         [:div {:class "box"}]
         (map (fn [i] (item-se3 conf i)) (range no)))]]
      (missing conf))))

(defn index
  [conf req]
  (let [s        (m/get-val! (k/standard conf))
        standard (if s s "~")]
    (hp/html5
     (page-header conf)
     [:body
      (index-title conf standard)
      (main-select conf)
      (condp = (keyword standard)
        :SE3 (items-se3 conf)
        :SE1 (not-implemented conf)
        :CE3 (not-implemented conf)
        (missing conf))
      (hp/include-js "/js/jquery-3.5.1.min.js")
      (hp/include-js "/js/ws.js")
      (hp/include-js "/js/main.js")])))

(defn device
  [conf req row]
  (hp/html5
   (page-header conf)
   [:body
    (device-title conf row)
    (device-select conf row)
    (when (m/get-val! (k/device conf row))
      (device-defaults! conf row))
    (hp/include-js "/js/jquery-3.5.1.min.js")
    (hp/include-js "/js/ws.js")
    (hp/include-js "/js/main.js")]))
