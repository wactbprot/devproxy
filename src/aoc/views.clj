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

(defn vl-select
  ([conf e-name init-vec]
   (vl-select conf e-name init-vec nil))
  ([conf e-name init-vec row]
   [:div {:class "column"}
    [:div {:class "field"}
    [:div {:class "control"}
     [:div
      (hf/drop-down {:id (if row (u/elem-id conf e-name row)  e-name)
                     :class (str "input is-primary " e-name)}
                    e-name init-vec)]]]]))

(defn device-link
  [conf row]
  [:div {:class "column is-1"}
   [:div {:class "field"}
    [:div {:class "control"}
      [:a {:href "https://google.com"}
       [:i {:class "fas fa-external-link-alt fa-2x"}]]]]])

(defn vl-button
  [id text]
  [:div {:class "column is-1"}
     [:div {:class "control"}
      (hf/submit-button {:id id :class "button is-primary"} text)]])

(defn index-top
  [conf]
  (let [standard   (m/get-val! (k/standard conf))
        year       (m/get-val! (k/year conf))
        maintainer (m/get-val! (k/maintainer conf))
        gas        (m/get-val! (k/gas conf))
        mode       (m/get-val! (k/mode conf))
        ]
  [:section {:class "section"}
   [:div {:class "container content"}
    [:div {:class "box"}
     [:div {:class "columns"}
      (vl-button "reset" "reset")
      (vl-select conf "standard"   (u/fill conf standard :standards))
      (vl-select conf "year"       (u/fill conf year :years))
      (vl-select conf "maintainer" (u/fill conf maintainer :maintainers))
      (vl-select conf "gas"        (u/fill conf gas :gases))
      (vl-select conf "mode"       (u/fill conf mode :modes))]]]]))

(defn index-title
  [page-type std conf]
  [:section {:class "hero is-info"}
   [:div {:class "hero-body"}
      [:div {:class "container"}
       [:h1 {:class "title"} (:main-title conf)]
       [:h2 {:class "subtitle"} (str "standard: " std)]]]])

(defn item-se3
  [conf row]
  (let [standard   (m/get-val! (k/standard conf))
        year       (m/get-val! (k/year conf))]
    (if (and standard year)
      [:div {:class "columns"}
       (vl-select conf "id" (db/cal-ids conf standard year) row)
       (vl-select conf "fullscale" (get-in conf [:se3 :items :fullscale]) row)
       (vl-select conf "branch"    (get-in conf [:se3 :items :branch]) row)
       (device-link conf row)])))
    
(defn items-se3
  "BTW:
  ```clojure
  (into [:div ] (range 9))
  ;; =>
  ;; [:div 0 1 2 3 4 5 6 7 8]
  ```
  "
  [conf]
  (let [standard   (m/get-val! (k/standard conf))
        year       (m/get-val! (k/year conf))]
    (if (and standard year)
      [:section {:class "section"}
       [:div {:class "container content"}
        (into
         [:div {:class "box"}]
         (map (fn [i] (item-se3 conf i))
              (range (get-in conf [:se3 :no-of-devs]))))
        ]]
      (missing conf))))

(defn index
  [page-type req conf]
  (let [standard (m/get-val! (k/standard conf))
        standard (if standard standard "~")]
    (hp/html5
     (page-header conf)
     [:body
      (index-title page-type standard  conf)
      (index-top  conf)
      (condp = (keyword standard)
        :SE3 (items-se3 conf)
        :SE1 (not-implemented conf)
        :CE3 (not-implemented conf)
        (missing conf))
      (hp/include-js "/js/jquery-3.5.1.min.js")
      (hp/include-js "/js/ws.js")
      (hp/include-js "/js/main.js")])))
  
