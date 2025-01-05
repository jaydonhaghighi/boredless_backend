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

function toggleButtonSelection(button, item, list) {
    const index = list.indexOf(item);

    if (index === -1) {
        list.push(item);
        button.classList.add("selected");
    } else {
        list.splice(index, 1);
        button.classList.remove("selected");
    }
}

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

        document.getElementById("countryName").textContent = selectedCountry;

        const cityList = document.getElementById("cityList");
        cityList.innerHTML = "";
        cities.forEach(city => {
            const button = document.createElement("button");
            button.classList.add("city-button");

            // Use structured innerHTML for better formatting
            button.innerHTML = `
                <strong>${city.name}</strong><br>
                <span class="city-description">${city.description}</span>
            `;

            button.onclick = () => {
                toggleButtonSelection(button, city, selectedCities);
                console.log("Selected Cities:", selectedCities);
            };

            cityList.appendChild(button);
        });

        document.getElementById("page1").classList.add("hidden");
        document.getElementById("page2").classList.remove("hidden");
    } catch (error) {
        console.error("Error in goToCitySelection:", error);
        alert("An error occurred while fetching cities.");
    }
}

function goToActivityTypeSelection() {
    if (selectedCities.length === 0) {
        alert("Please select at least one city.");
        return;
    }

    const activityTypeList = document.getElementById("activityTypeList");
    activityTypeList.innerHTML = "";

    activityTypeOptions.forEach(type => {
        const button = document.createElement("button");
        button.textContent = type;
        button.classList.add("activity-button");
        button.onclick = () => {
            toggleButtonSelection(button, type, selectedActivityTypes);
            console.log("Selected Activity Types:", selectedActivityTypes);
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

    console.log("Selected Activity Types:", selectedActivityTypes);

    // Clear the #activityList container (Page 4) before populating
    const activityList = document.getElementById("activityList");
    activityList.innerHTML = "";

    // For each city selected, fetch activities and build a column
    for (const city of selectedCities) {
        console.log("Fetching activities for city:", city.name);

        const payload = { city, activityTypes: selectedActivityTypes };

        try {
            const response = await fetch(`/api/v1/generate/activities`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload)
            });

            console.log("Response Status:", response.status);

            if (!response.ok) {
                console.error("Error fetching activities:", response.statusText);
                continue;
            }

            const activities = await response.json();
            console.log("Activities Response:", activities);

            // Skip if no activities returned
            if (!Array.isArray(activities) || activities.length === 0) {
                continue;
            }

            // Create a dedicated "column" div for this city
            const cityColumn = document.createElement("div");
            cityColumn.classList.add("city-activities-column");

            // Add a header with the city name
            const cityHeader = document.createElement("h3");
            cityHeader.textContent = city.name;
            cityColumn.appendChild(cityHeader);

            // Initialize the object to store selected activities for this city
            selectedActivities[city.name] = {};

            // For each selected activity type, create a vertical list of buttons
            selectedActivityTypes.forEach(type => {
                // Filter activities for this type
                const typeActivities = activities.filter(a => a.type === type);

                // Prepare an array to store selected items
                selectedActivities[city.name][type] = [];

                // Create a container for the activity type (header + button stack)
                const typeContainer = document.createElement("div");
                typeContainer.classList.add("activity-type-container");

                // Activity type header
                const typeHeader = document.createElement("h4");
                typeHeader.textContent = type;
                typeContainer.appendChild(typeHeader);

                // Vertical stack for activity buttons
                const activityButtons = document.createElement("div");
                activityButtons.classList.add("activity-buttons");

                // Create a button for each activity
                typeActivities.forEach(activity => {
                    const button = document.createElement("button");
                    button.textContent = activity.title;
                    button.classList.add("activity-button");
                    button.onclick = () => {
                        toggleButtonSelection(button, activity, selectedActivities[city.name][type]);
                        console.log(`Selected Activities for ${type} in ${city.name}:`, selectedActivities[city.name][type]);
                    };
                    activityButtons.appendChild(button);
                });

                // Combine the buttons into the type container, and the container into the city column
                typeContainer.appendChild(activityButtons);
                cityColumn.appendChild(typeContainer);
            });

            // Finally, add the city column to the #activityList container
            activityList.appendChild(cityColumn);

        } catch (error) {
            console.error(`Error fetching activities for city ${city.name}:`, error);
        }
    }

    // Show Page 4, hide Page 3
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

        const activitiesByType = selectedActivities[city]; // This is an object with activity types as keys.

        for (const [type, activities] of Object.entries(activitiesByType)) {
            const typeHeader = document.createElement("h4");
            typeHeader.textContent = type;
            summaryDiv.appendChild(typeHeader);

            activities.forEach(activity => {
                const activityItem = document.createElement("div");
                activityItem.innerHTML = `
                    <strong>${activity.title}</strong><br>
                    <small>Best Time: ${activity.bestTime}</small><br>
                    <small>Cost: ${activity.cost}</small>
                `;
                activityItem.classList.add("summary-activity");
                summaryDiv.appendChild(activityItem);
            });
        }
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
