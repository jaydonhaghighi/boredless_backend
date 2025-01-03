let selectedCountry = "";
let selectedCities = []; // Array of city objects {name, country, description, population}
let selectedActivityTypes = [];
let selectedActivities = {};

const activityTypeOptions = [
    "Hike", "Wine Tasting", "Beach", "Drinking", "Partying",
    "Shopping", "Camping", "Dining", "Breakfast", "Cafe",
    "SightSeeing", "Surfing", "Boat Tour", "Tour", "Architecture",
    "Snorkeling", "Museum Visit", "Nature Walk", "Photography", "Adventure Sports"
];

async function goToCitySelection() {
    selectedCountry = document.getElementById("countryInput").value.trim();
    console.log("Selected Country:", selectedCountry);

    if (!selectedCountry) {
        alert("Please enter a country.");
        return;
    }

    const url = `/api/v1/generate/top-cities?country=${encodeURIComponent(selectedCountry)}`;
    console.log("Fetching URL:", url);

    try {
        const response = await fetch(url);
        console.log("Response Status:", response.status);

        if (!response.ok) {
            console.error("Error fetching cities:", response.statusText);
            alert("Failed to fetch cities. Please try again.");
            return;
        }

        const cities = await response.json();
        console.log("Cities Response:", cities);

        if (!Array.isArray(cities) || cities.length === 0) {
            alert("No cities found for this country.");
            return;
        }

        // Update country name dynamically
        document.getElementById("countryName").textContent = selectedCountry;

        const cityList = document.getElementById("cityList");
        cityList.innerHTML = "";
        cities.forEach(city => {
            const container = document.createElement("div");
            container.classList.add("checkbox");

            const checkbox = document.createElement("input");
            checkbox.type = "checkbox";
            checkbox.id = city.name;
            checkbox.value = JSON.stringify(city);

            const label = document.createElement("label");
            label.htmlFor = city.name;
            label.textContent = `${city.name} (${city.population}) - ${city.description}`;

            container.appendChild(checkbox);
            container.appendChild(label);
            cityList.appendChild(container);
        });

        console.log("City List HTML:", cityList.innerHTML);

        document.getElementById("page1").classList.add("hidden");
        document.getElementById("page2").classList.remove("hidden");
    } catch (error) {
        console.error("Error in goToCitySelection:", error);
        alert("An error occurred while fetching cities.");
    }
}

function goToActivityTypeSelection() {
    selectedCities = Array.from(document.querySelectorAll("#cityList input:checked")).map(input => JSON.parse(input.value));
    console.log("Selected Cities:", selectedCities); // Debug: Log selected cities

    if (selectedCities.length === 0) {
        alert("Please select at least one city.");
        return;
    }

    const activityTypeList = document.getElementById("activityTypeList");
    activityTypeList.innerHTML = "";

    activityTypeOptions.forEach(type => {
        const button = document.createElement("div");
        button.classList.add("activity-button");
        button.textContent = type;
        button.onclick = () => {
            if (!selectedActivityTypes.includes(type)) {
                selectedActivityTypes.push(type);
                button.style.backgroundColor = "#c2e7da"; // Highlight selected
            } else {
                selectedActivityTypes = selectedActivityTypes.filter(t => t !== type);
                button.style.backgroundColor = ""; // Unselect
            }
        };
        activityTypeList.appendChild(button);
    });

    document.getElementById("page2").classList.add("hidden");
    document.getElementById("page3").classList.remove("hidden");
}

async function goToActivitySelection() {
    if (selectedActivityTypes.length === 0) {
        alert("Please select at least one activity type.");
        return;
    }

    console.log("Selected Activity Types:", selectedActivityTypes); // Debug

    const activityList = document.getElementById("activityList");
    activityList.innerHTML = "";

    for (const city of selectedCities) {
        console.log("Fetching activities for city:", city.name); // Debug

        const payload = {
            city,
            activityTypes: selectedActivityTypes
        };
        console.log("Payload:", payload); // Debug

        try {
            const response = await fetch(`/api/v1/generate/activities`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload)
            });

            console.log("Response Status:", response.status); // Debug

            if (!response.ok) {
                console.error("Error fetching activities:", response.statusText);
                continue;
            }

            const activities = await response.json();
            console.log("Activities Response:", activities); // Debug

            if (!Array.isArray(activities) || activities.length === 0) continue;

            const cityHeader = document.createElement("h3");
            cityHeader.textContent = city.name;
            activityList.appendChild(cityHeader);

            selectedActivities[city.name] = [];

            activities.forEach(activity => {
                const button = document.createElement("div");
                button.classList.add("activity-button");
                button.innerHTML = `
                    <strong>${activity.title}</strong><br>
                    <small>Type: ${activity.type}</small><br>
                    <small>Best Time: ${activity.bestTime}</small><br>
                    <small>Cost: ${activity.cost}</small>
                `;
                button.onclick = () => {
                    if (!selectedActivities[city.name].some(a => a.title === activity.title)) {
                        selectedActivities[city.name].push(activity);
                    }
                };
                activityList.appendChild(button);
            });
        } catch (error) {
            console.error(`Error fetching activities for city ${city.name}:`, error); // Debug
        }
    }

    document.getElementById("page3").classList.add("hidden");
    document.getElementById("page4").classList.remove("hidden");
}

function goToSummary() {
    const summaryDiv = document.getElementById("summary");
    summaryDiv.innerHTML = "";

    Object.keys(selectedActivities).forEach(city => {
        const cityHeader = document.createElement("h3");
        cityHeader.textContent = city;
        summaryDiv.appendChild(cityHeader);

        selectedActivities[city].forEach(activity => {
            const activityItem = document.createElement("div");
            activityItem.classList.add("summary-activity");
            activityItem.innerHTML = `
                <strong>${activity.title}</strong><br>
                <small>Type: ${activity.type}</small><br>
                <small>Best Time: ${activity.bestTime}</small><br>
                <small>Cost: ${activity.cost}</small>
            `;
            summaryDiv.appendChild(activityItem);
        });
    });

    document.getElementById("page4").classList.add("hidden");
    document.getElementById("page5").classList.remove("hidden");
}

function startOver() {
    selectedCountry = "";
    selectedCities = [];
    selectedActivityTypes = [];
    selectedActivities = {};

    document.getElementById("countryInput").value = "";
    document.getElementById("cityList").innerHTML = "";
    document.getElementById("activityList").innerHTML = "";
    document.getElementById("summary").innerHTML = "";

    document.getElementById("page5").classList.add("hidden");
    document.getElementById("page1").classList.remove("hidden");
}
