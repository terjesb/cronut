(ns troy-west.cronut.integration-fixture
  (:require [clojure.test :refer :all]
            [clojure.java.io :as io]
            [troy-west.cronut :as cronut]
            [integrant.core :as ig]
            [clojure.tools.logging :as log])
  (:import (org.quartz Job)))

(defonce system (atom {}))

(defrecord TestDefrecordJobImpl [identity description recover? durable? test-dep]
  Job
  (execute [this job-context]
    (log/info "Defrecord Impl:" this)))

(defmethod ig/init-key :dep/one
  [_ config]
  config)

(defmethod ig/init-key :test.job/one
  [_ config]
  (reify Job
    (execute [this job-context]
      (log/info "Reified Impl:" config))))

(defmethod ig/init-key :test.job/two
  [_ config]
  (map->TestDefrecordJobImpl config))

(defn initialize!
  []
  (reset! system (cronut/init-system (slurp (io/resource "config.edn")))))

(defn shutdown!
  []
  (swap! system cronut/halt-system))

(defn wrap-test
  [test-fn]
  (initialize!)
  (test-fn)
  (shutdown!))