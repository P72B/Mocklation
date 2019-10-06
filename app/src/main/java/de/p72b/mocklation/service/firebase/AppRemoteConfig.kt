package de.p72b.mocklation.service.firebase

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class AppRemoteConfig(
        @SerializedName("appCredits")
        @Expose
        var appCredits: AppCredits = AppCredits(),

        @SerializedName("privacyUrl")
        @Expose
        var privacyUrl: String = ""
)

data class AppCredits(
        @SerializedName("githubProjectRepositoryUrl")
        @Expose
        var githubProjectRepositoryUrl: String = "",

        @SerializedName("producer")
        @Expose
        var producer: Producer = Producer(),

        @SerializedName("contributors")
        @Expose
        var contributors: List<Producer> = mutableListOf()
)

data class Producer(
        @SerializedName("email")
        @Expose
        var email: String = "",

        @SerializedName("name")
        @Expose
        var name: String = "",

        @SerializedName("country")
        @Expose
        var country: String = "",

        @SerializedName("city")
        @Expose
        var city: String = ""
)
