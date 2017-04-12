;; The MIT License (MIT)

;; Copyright (c) 2016 Stuart Sierra

;; Permission is hereby granted, free of charge, to any person
;; obtaining a copy of this software and associated documentation
;; files (the "Software"), to deal in the Software without
;; restriction, including without limitation the rights to use, copy,
;; modify, merge, publish, distribute, sublicense, and/or sell copies
;; of the Software, and to permit persons to whom the Software is
;; furnished to do so, subject to the following conditions:

;; The above copyright notice and this permission notice shall be
;; included in all copies or substantial portions of the Software.

;; THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
;; EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
;; MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
;; NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
;; BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
;; ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
;; CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
;; SOFTWARE.

(ns com.stuartsierra.mapgraph.spec
  "clojure.spec definitions for mapgraph functions.
  Requires Clojure 1.9.0."
  (:require [clojure.spec :as s]
            [com.stuartsierra.mapgraph :as mg]))

(s/def ::mg/db mg/db?)

(s/def ::mg/entity (s/map-of keyword? ::s/any))

(s/def ::mg/reference
  (s/and vector?
         (s/tuple keyword? ::s/any)))

(s/def ::mg/lookup-ref ::mg/reference)

(s/def ::mg/pattern
  (s/* (s/or :attr keyword?
             :star #{'*}
             :join (s/map-of keyword? ::mg/pattern))))

(s/def ::mg/result (s/nilable map?))

(s/def ::mg/parser-context
  (s/keys :req-un [::mg/parser ::mg/db ::mg/lookup-ref]
          :opt-un [::mg/pattern ::mg/entity]))

(s/def ::mg/parser
  (s/fspec :args (s/cat :context ::mg/parser-context
                        :result ::mg/result
                        :pattern ::mg/pattern)
           :ret ::mg/result))

(s/fdef mg/add-id-attr
  :args (s/cat :db ::mg/db
               :idents (s/* keyword?))
  :ret ::mg/db)

(s/fdef mg/add
  :args (s/& (s/cat :db ::mg/db
                    :entities (s/* ::mg/entity))
             (fn [{:keys [db entities]}]
               (every? #(mg/entity? db %) entities)))
  :ret ::mg/db
  :fn (fn [{:keys [ret args]}]
        (let [{:keys [db entities]} args]
          (every? #(contains? ret (mg/ref-to db %)) entities))))

(s/fdef mg/pull
  :args (s/or :default
              (s/&
               (s/cat :db ::mg/db
                      :pull (s/spec ::mg/pattern)
                      :ref ::mg/reference)
               (fn [{:keys [db ref]}]
                 (mg/ref? db ref)))
              :pull-ref-fn
              (s/&
               (s/cat :db ::mg/db
                      :parser ::mg/parser
                      :patter (s/spec ::mg/pattern)
                      :ref ::mg/reference)
               (fn [{:keys [db ref]}]
                 (mg/ref? db ref))))
  :ret (s/nilable map?))

;; Local Variables:
;; eval: (put-clojure-indent 'fdef :defn)
;; End:
