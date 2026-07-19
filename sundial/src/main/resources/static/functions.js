 let map;
    let cafeResults = [];

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
        [out:json][timeout:60];
        node["amenity"="cafe"](around:2000,${lat},${lon});
        out body;
    `;

    const url = "https://overpass-api.de/api/interpreter?data="
                + encodeURIComponent(query);

    fetch(url)
        .then(response => {
            if (!response.ok) throw new Error("Status " + response.status);
            return response.json();
        })
        .then(data => {
            if (!data.elements || data.elements.length === 0) {
                document.getElementById("loadingMsg").textContent =
                    "No cafés found within 2km.";
                return;
            }

            document.getElementById("loadingMsg").textContent =
                "☀️ Checking sun for " + data.elements.length + " cafés...";

            const cafes = data.elements.map(cafe => ({
                name:         cafe.tags.name         || "Unnamed Café",
                description:  cafe.tags.description  || "",
                openingHours: cafe.tags.opening_hours || "",
                latitude:     cafe.lat,
                longitude:    cafe.lon
            }));

            checkCafesWithBackend(cafes);
        })
        .catch(error => {
            document.getElementById("loadingMsg").textContent =
                "⚠️ Could not load café data. Try again in a moment.";
            document.getElementById("loadingMsg").className = "alert alert-warning mt-3";
            console.error("Overpass error:", error);
        });
}

    function checkCafesWithBackend(cafes) {
        fetch("/cafes/check", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(cafes)
        })
        .then(response => response.json())
        .then(enrichedCafes => {
            cafeResults = enrichedCafes;
            document.getElementById("loadingMsg").style.display = "none";
            document.getElementById("legend").style.display = "block";

            enrichedCafes.forEach(cafe => addCafeMarker(cafe));
            renderCafeList(enrichedCafes);
        })
        .catch(error => {
            document.getElementById("loadingMsg").textContent =
                "⚠️ Could not check weather for cafés.";
            console.error(error);
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
                ${cafe.openingHours ? "🕐 " + cafe.openingHours + "<br>" : ""}
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
                <td>${cafe.openingHours || "<i class='text-muted'>Unknown</i>"}</td>
                <td>
                    <select class="form-control form-control-sm"
                            onchange="submitRating(${index}, this.value)">
                        <option value="0">Rate...</option>
                        <option value="1">⭐</option>
                        <option value="2">⭐⭐</option>
                        <option value="3">⭐⭐⭐</option>
                        <option value="4">⭐⭐⭐⭐</option>
                        <option value="5">⭐⭐⭐⭐⭐</option>
                    </select>
                </td>
                <td>${cafe.sunny ? "☀️ Sunny" : "🌥️ Cloudy"}</td>
            `;
            container.appendChild(row);
        });

        document.getElementById("cafeTable").style.display = "block";
    }

    function submitRating(index, rating) {
        if (rating == 0) return;

        const cafe = cafeResults[index];
        cafe.rating = parseInt(rating);

        fetch("/cafes/rate", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(cafe)
        })
        .then(response => {
            if (response.ok) {
                console.log("Rating saved for " + cafe.name);
            }
        })
        .catch(error => console.error("Rating save failed:", error));
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