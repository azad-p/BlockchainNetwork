import pandas as pd
from sklearn import metrics
from sklearn.model_selection import train_test_split
from sklearn.ensemble import RandomForestClassifier
import statistics

# Creating a DataFrame of dataset
data = pd.read_csv('featureExtractionFinal.csv', low_memory=False)
X = data[['AmountSent', 'Income', 'Neighbours', 'CoAddresses', 'Successors']]  # Features
y = data['IsRansome']  # Labels

# Now, we simulate 'time' by training and testing on the next address incrementally

accuracy = []
print_every_x = 5000

i = 0
while i < len(data):
    i += 2
    subSetX = X[:i]
    subSetY = y[:i]

    # Split dataset into training set and test set
    X_train, X_test, y_train, y_test = train_test_split(subSetX, subSetY, test_size=0.5)

    # Create a Classifier
    clf = RandomForestClassifier(n_estimators=50)

    # Train the model using the training sets
    clf.fit(X_train, y_train)

    y_pred = clf.predict(X_test)
    accuracy.append(metrics.accuracy_score(y_test, y_pred))

    if i % print_every_x == 0:
        print('reached i: ' + str(i))

print(statistics.mean(accuracy))
