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

(defn vl-select
  [name init-vec]
  [:div {:class "column"}
   [:div {:class "field"}
    [:div {:class "control"}
     [:div (hf/drop-down {:id name :class "input is-primary"} name init-vec)]]]])

(defn vl-button
  [id text]
  [:div {:class "column"}
     [:div {:class "control"}
      (hf/submit-button {:id id :class "button is-primary"} text)]])

(defn index-top
  [conf]
  [:section {:class "section"}
   [:div {:class "container is-fluid"}
    [:div {:class "columns"}
     (vl-select "year" (:years conf))
     (vl-select "maintainer" (:maintainers conf))
     (vl-select "gas" (:gases conf))
     (vl-select "mode" (:modes conf))]]])

(defn index-title
  [conf]
  [:section {:class "hero is-info"}
   [:div {:class "hero-body"}
      [:div {:class "container"}
       [:h1 {:class "title"} (:main-title conf)]]]])

(defn items-se3
  [conf]
  [:section {:class "section"}
   [:div {:class "container is-fluid"}
    [:div {:class "columns"}
     (vl-select "id" ["a" "b"])
     (vl-select "fullscale" ["0.1T" "1T"])
     (vl-select "branch" ["a" "b" "c"])
     [:i {:class "fas fa-external-link-alt fa-3x"}]  
     ]]])

(defn index
  [std conf]
  (hp/html5
   (page-header conf)
   [:body
    (index-title conf)
    (index-top conf)
    (condp = (keyword std)
      :se3 (items-se3 conf)
      (not-found))
    
    ]))
