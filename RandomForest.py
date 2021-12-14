import pandas as pd
from sklearn import metrics
from sklearn.model_selection import train_test_split
from sklearn.ensemble import RandomForestClassifier
import statistics

# Creating a DataFrame of dataset
data = pd.read_csv('featureExtractionFinal.csv', low_memory=False)
X = data[['AmountSent', 'Income', 'Neighbours', 'CoAddresses', 'Successors']]  # Features
Day_index = data['Day']
y = data['IsRansome']  # Labels

# Organize the data to index it based on when the days change
# This way, we run random forest on each day
previous_day = 1
indexes_of_next_days = []
for i in range(len(Day_index)):
    if Day_index[i] != previous_day:
        previous_day = Day_index[i]
        indexes_of_next_days.append(i)

# Now, we simulate 'time' by training and testing on the next address incrementally
print(len(indexes_of_next_days))

accuracy = []
print_every_x = 5000

# Create a Classifier
clf = RandomForestClassifier(n_estimators=50)

day = 0
for i in indexes_of_next_days:
    day += 1
    subSetX = X[:i]
    subSetY = y[:i]

    # Split dataset into training set and test set
    X_train, X_test, y_train, y_test = train_test_split(subSetX, subSetY, test_size=0.3)

    # Train the model using the training sets
    clf.fit(X_train, y_train)

    y_pred = clf.predict(X_test)
    accuracy.append(metrics.accuracy_score(y_test, y_pred))

    if day % 30 == 0:
        print('Progress report: on day ' + str(day))

print('average accuracy: ' + str(statistics.mean(accuracy)))
print('~~~~ printing accuracies for all days ~~~~')

for i in range(len(accuracy)):
    print('Day: ' + str(i) + ', accuracy = ' + str(accuracy[i]))
