package com.plexobject.storage.domain

import java.lang.RuntimeException

class NotFoundException(msg: String) : RuntimeException(msg) {
}