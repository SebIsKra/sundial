 let map;
    let cafeResults = [];
    let editingCafeId = null;

    function getLocation() {
        if (!navigator.geolocation) {
            document.getElementById("geoError").style.display = "block";
            return;
        }

        navigator.geolocation.getCurrentPosition(
            function(position) {
                const lat = position.coords.latitude;
                const lon = position.coords.longitude;

                document.getElementById("lat").value = lat.toFixed(4);
                document.getElementById("lon").value = lon.toFixed(4);
                document.getElementById("geoError").style.display = "none";
                document.getElementById("loadingMsg").style.display = "block";
                document.getElementById("map").style.display = "block";

                initMap(lat, lon);
                fetchCafes(lat, lon);
            },
            function(error) {
                document.getElementById("geoError").style.display = "block";
            }
        );
    }

    function initMap(lat, lon) {
        if (map) {
            map.remove();
        }

        map = L.map("map").setView([lat, lon], 14);

        L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
            attribution: "© OpenStreetMap contributors"
        }).addTo(map);

        L.marker([lat, lon])
            .addTo(map)
            .bindPopup("📍 You are here")
            .openPopup();

        L.circle([lat, lon], {
            radius: 2000,
            color: "#3388ff",
            fillOpacity: 0.1
        }).addTo(map);
    }

    /*function fetchCafes(lat, lon) {
        document.getElementById("loadingMsg").textContent =
            "🔍 Finding cafés nearby...";
        document.getElementById("loadingMsg").style.display = "block";

        // Mock café data — swap for Overpass API when network allows
        const mockCafes = [
            { name: "Café Einstein Stammhaus", latitude: lat + 0.005, longitude: lon + 0.005, description: "", openingHours: "" },
            { name: "The Barn Coffee Roasters", latitude: lat - 0.003, longitude: lon + 0.008, description: "", openingHours: "" },
            { name: "Bonanza Coffee",           latitude: lat + 0.008, longitude: lon - 0.004, description: "", openingHours: "" },
            { name: "Five Elephant",            latitude: lat - 0.006, longitude: lon - 0.006, description: "", openingHours: "" },
            { name: "Café Himmelblau",          latitude: lat + 0.010, longitude: lon + 0.002, description: "", openingHours: "" }
        ];

        checkCafesWithBackend(mockCafes);
    }*/
    function fetchCafes(lat, lon) {
    document.getElementById("loadingMsg").textContent =
        "🔍 Finding cafés nearby...";
    document.getElementById("loadingMsg").style.display = "block";

    const query = `
        [out:json][timeout:25];
        node["amenity"="cafe"](around:2000,${lat},${lon});
        out body 20;
    `;

    const url = "https://overpass-api.de/api/interpreter?data="
                + encodeURIComponent(query);

    // Abort after 20 seconds
    const controller = new AbortController();
    const timeout = setTimeout(() => controller.abort(), 20000);

    fetch(url, { signal: controller.signal })
        .then(response => {
            clearTimeout(timeout);
            if (!response.ok) throw new Error("Status " + response.status);
            return response.json();
        })
        .then(data => {
            if (!data.elements || data.elements.length === 0) {
                throw new Error("No results");
            }

            document.getElementById("loadingMsg").textContent =
                "☀️ Checking sun for " + data.elements.length + " cafés...";

            const cafes = data.elements.map(cafe => ({
                name:        cafe.tags.name        || "Unnamed Café",
                description: "",
                latitude:    cafe.lat,
                longitude:   cafe.lon,
                address: getAddress(cafe.tags)
            }));

            checkCafesWithBackend(cafes);
        })
        .catch(error => {
            clearTimeout(timeout);
            console.warn("Overpass failed, using mock data:", error.message);

            // Fall back to mock data automatically
            const mockCafes = [
                { name: "Café Einstein Stammhaus", latitude: lat + 0.005, longitude: lon + 0.005, description: "" },
                { name: "The Barn Coffee Roasters", latitude: lat - 0.003, longitude: lon + 0.008, description: "" },
                { name: "Bonanza Coffee",           latitude: lat + 0.008, longitude: lon - 0.004, description: "" },
                { name: "Five Elephant",            latitude: lat - 0.006, longitude: lon - 0.006, description: "" },
                { name: "Café Himmelblau",          latitude: lat + 0.010, longitude: lon + 0.002, description: ""}
            ];

            document.getElementById("loadingMsg").textContent =
                "☀️ Checking sun for cafés...";

            checkCafesWithBackend(mockCafes);
        });
}

   function checkCafesWithBackend(cafes) {
    console.log("Sending " + cafes.length + " cafés to backend");

    fetch("/cafes/check", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(cafes)
    })
    .then(response => {
        if (!response.ok) throw new Error("Server error: " + response.status);
        return response.json();
    })
    .then(enrichedCafes => {
        console.log("Backend returned " + enrichedCafes.length + " cafés");

        cafeResults = enrichedCafes;
        document.getElementById("loadingMsg").style.display = "none";
        document.getElementById("legend").style.display = "block";

        enrichedCafes.forEach(cafe => addCafeMarker(cafe));
        renderCafeList(enrichedCafes);
    })
    .catch(error => {
        document.getElementById("loadingMsg").textContent =
            "⚠️ Could not check weather for cafés.";
        console.error("checkCafesWithBackend error:", error);
    });
}

    function addCafeMarker(cafe) {
        const isSunny = cafe.sunny;
        const icon = createColoredIcon(isSunny ? "green" : "grey");

        L.marker([cafe.latitude, cafe.longitude], { icon })
            .addTo(map)
            .bindPopup(`
                <b>${cafe.name}</b><br>
                ☁️ Cloudiness: ${cafe.cloudiness}%<br>
                ${isSunny ? "☀️ Sunny spot!" : "🌥️ Too cloudy"}
            `);
    }

    function renderCafeList(cafes) {
    const container = document.getElementById("cafeList");
    container.innerHTML = "";

    cafes.forEach((cafe, index) => {
        const row = document.createElement("tr");
        row.innerHTML = `
            <td><b>${cafe.name}</b></td>
            <td>${cafe.description || "<i class='text-muted'>No description yet</i>"}</td>
            <td>${cafe.address || "<i>No address available</i>"}</td>
            <td class="text-center">
                <span id="heart-${index}"
                      onclick="toggleFavourite(${index})"
                      style="cursor:pointer; font-size:1.4rem;"
                      title="Add to favourites">
                    ${cafe.favourite ? "❤️" : "🤍"}
                </span>
            </td>
            <td>${cafe.sunny ? "☀️ Sunny" : "🌥️ Cloudy"}</td>
            <td class="text-center"><button class="btn btn-sm btn-edit" onclick="openEditPopup(${cafe.id})"> ✏️ </button></td>

        `;
        container.appendChild(row);
    });

    document.getElementById("cafeTable").style.display = "block";
}

    function toggleFavourite(index) {
    const cafe = cafeResults[index];
    cafe.favourite = !cafe.favourite;

    // Update the heart icon visually
    const heart = document.getElementById("heart-" + index);
    heart.textContent = cafe.favourite ? "❤️" : "🤍";

    fetch("/cafes/favourite", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(cafe)
    })
    .then(response => {
        if (!response.ok) {
            // Revert if save failed
            cafe.favourite = !cafe.favourite;
            heart.textContent = cafe.favourite ? "❤️" : "🤍";
            console.error("Failed to save favourite");
        }
    })
    .catch(error => {
        // Revert on network error
        cafe.favourite = !cafe.favourite;
        heart.textContent = cafe.favourite ? "❤️" : "🤍";
        console.error("Favourite save failed:", error);
    });
}

    function createColoredIcon(color) {
        return L.divIcon({
            className: "",
            html: `<div style="
                width: 16px;
                height: 16px;
                background: ${color};
                border-radius: 50%;
                border: 2px solid white;
                box-shadow: 0 0 4px rgba(0,0,0,0.4);
            "></div>`,
            iconSize: [16, 16],
            iconAnchor: [8, 8]
        });
    }

    function getAddress(tags) {
    const parts = [
        tags["addr:housenumber"],
        tags["addr:street"],
        tags["addr:city"],
        tags["addr:postcode"]
    ].filter(Boolean);

    // Require street + city
    if (!tags["addr:street"] || !tags["addr:city"]) {
        return null;
    }

    return parts.join(" ");
    }

    function openEditPopup(cafeId) {

    editingCafeId = cafeId;

    const cafe = cafeResults.find(c => c.id == cafeId);

    if (!cafe) {
        console.error("Cafe not found:", cafeId);
        return;
    }

    document.getElementById("editCafeId").value = cafe.id;
    document.getElementById("editCafeName").value = cafe.name;
    document.getElementById("editCafeDescription").value =
        cafe.description || "";

    $("#editDescriptionModal").modal("show");
}


function saveDescription() {

    const cafe = cafeResults.find(c => c.id == editingCafeId);

    if (!cafe) {
        return;
    }

    cafe.description =
        document.getElementById("editCafeDescription").value;


    fetch("/cafes/description", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(cafe)
    })
    .then(response => {

        if (!response.ok) {
            throw new Error("Save failed");
        }

        $("#editDescriptionModal").modal("hide");

        renderCafeList(cafeResults);
    })

    .catch(error => {
        console.error(error);
        alert("Could not save description");
    });
}


   