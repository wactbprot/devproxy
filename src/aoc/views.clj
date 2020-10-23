(ns aoc.views
  (:require
   [hiccup.page :as hp]
   [hiccup.form :as hf]))

(defn page-header
  [conf]
  [:head
   [:title (:page-title conf)]
   (hp/include-css "/css/bulma.css")
   (hp/include-css "/css/all.css")])

(defn not-found []
  (hp/html5
    [:h1 "404 Error!"]
    [:b "Page not found!"]
    [:p [:a {:href ".."} "Return to main page"]]))

(defn elem-id
  [conf a b]
  (str a (:elem-sep conf) b))

(defn vl-select
  ([conf name init-vec]
   (vl-select conf name init-vec nil))
  ([conf name init-vec row]
   [:div {:class "column"}
    [:div {:class "field"}
    [:div {:class "control"}
     [:div (hf/drop-down {:id (if row (elem-id conf name row)  name)
                          :class (str "input is-primary " name)}
                         name init-vec)]]]]))

(defn device-link
  [conf row]
  [:div {:class "column"}
   [:div {:class "field"}
    [:div {:class "control"}
      [:a {:href "https://google.com"}
       [:i {:class "fas fa-external-link-alt fa-2x"}]]]]])

(defn vl-button
  [id text]
  [:div {:class "column"}
     [:div {:class "control"}
      (hf/submit-button {:id id :class "button is-primary"} text)]])

(defn index-top
  [conf]
  [:section {:class "section"}
   [:div {:class "container content"}
    [:div {:class "columns"}
     (vl-select conf "year" (:years conf))
     (vl-select conf "maintainer" (:maintainers conf))
     (vl-select conf "gas" (:gases conf))
     (vl-select conf "mode" (:modes conf))]]])

(defn index-title
  [page-type std conf]
  [:section {:class "hero is-info"}
   [:div {:class "hero-body"}
      [:div {:class "container"}
       [:h1 {:class "title"} (:main-title conf)]
       [:h2 {:class "subtitle"} (str "standard: " std)]]]])

(defn item-se3
  [conf row]
  [:div {:class "columns"}
   (vl-select conf "id" [] row)
   (vl-select conf "fullscale" (get-in conf [:se3 :items :fullscale] row))
   (vl-select conf "branch"    (get-in conf [:se3 :items :branch]) row)
   (device-link conf row)])
  
(defn items-se3
  [conf]
  [:section {:class "section"}
   [:div {:class "container content"}
    (item-se3 conf 0)
    (item-se3 conf 1)
    (item-se3 conf 2)
    (item-se3 conf 3)
    (item-se3 conf 4)
     ]])

(defn index
  [page-type std conf]
  (hp/html5
   (page-header conf)
   [:body
    (index-title page-type std  conf)
    (index-top  conf)
    (condp = (keyword std)
      :se3 (items-se3 conf)
      (not-found))
    
    ]))
