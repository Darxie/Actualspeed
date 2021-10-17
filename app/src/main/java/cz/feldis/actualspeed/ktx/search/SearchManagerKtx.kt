package cz.feldis.actualspeed.ktx.search

import com.sygic.sdk.search.SearchManager
import com.sygic.sdk.search.SearchManagerProvider
import com.sygic.sdk.search.Session
import cz.feldis.actualspeed.ktx.SdkManagerKtx

class SearchManagerKtx : SdkManagerKtx<SearchManager>(SearchManagerProvider::getInstance) {

    suspend fun newOnlineSession() : Session = manager().newOnlineSession()
    suspend fun closeSession(session: Session) = manager().closeSession(session)
}