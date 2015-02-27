(ns pe-apptxn-restsupport.test-utils
  (:require [pe-apptxn-restsupport.meta :as meta]
            [pe-rest-utils.core :as rucore]
            [pe-rest-utils.meta :as rumeta]))

(def apptxn-schema-files ["apptxn-logging-schema-updates-0.0.1.dtm"])

(def db-uri "datomic:mem://apptxns")

(def apptxn-partition
  "The name of the Datomic partition of the application transactions."
  :apptxn)
(def apptxnmt-subtype-prefix "vnd.")
(def apptxnhdr-auth-token "apptxn-rest-auth-token")
(def apptxnhdr-error-mask "apptxn-rest-error-mask")
(def apptxnhdr-apptxn-id "apptxn-id")
(def apptxnhdr-useragent-device-make "apptxn-rest-useragent-device-make")
(def apptxnhdr-useragent-device-os "apptxn-rest-useragent-device-os")
(def apptxnhdr-useragent-device-os-version "apptxn-rest-useragent-device-os-version")
(def base-url "")
(def entity-uri-prefix "/testing/")
(def entity-uri-template (format "%s%s%s"
                                 base-url
                                 entity-uri-prefix
                                 meta/pathcomp-apptxnset))
