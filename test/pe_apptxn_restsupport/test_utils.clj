(ns pe-apptxn-restsupport.test-utils
  (:require [pe-apptxn-restsupport.resource-support :as ressupport]
            [pe-apptxn-restsupport.meta :as meta]
            [pe-rest-utils.core :as rucore]
            [pe-rest-utils.meta :as rumeta]
            [liberator.core :refer [defresource]]))

(def apptxn-schema-filename "apptxn-logging-schema.dtm")

(def db-uri "datomic:mem://apptxns")

(def apptxn-partition
  "The name of the Datomic partition of the application transactions."
  :apptxn)

(def apptxns-test-url (format "/testing/%s" meta/pathcomp-apptxnset))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Concrete Resource (for testing)
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defresource apptxnset-res [conn]
  :available-media-types (rucore/enumerate-media-types
                          meta/supported-media-types)
  :available-charsets rumeta/supported-char-sets
  :available-languages rumeta/supported-languages
  :allowed-methods [:post]
  :known-content-type? (rucore/known-content-type-predicate meta/supported-media-types)
  :new? false ; so we return a 204 instead of a 201
  :post! (fn [ctx] (ressupport/handle-apptxnset-post! ctx conn apptxn-partition nil nil nil))
  :handle-created rucore/handle-resp)
