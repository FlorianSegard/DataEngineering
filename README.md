# Data Engineering Project

## Question 1: Statistics

### Technical and Business Constraints for Data Storage:

- **Data Consistency**: Data for statistics must be consistent and accurate to ensure long-term analysis integrity.
- **Data Integrity**: Stored data must be complete and error-free to ensure the reliability of statistical analysis.
- **Security**: Sensitive individual data must be secured to comply with confidentiality and regulatory requirements.
- **Scalability**: The system must be able to scale to handle increased data volume without performance loss.
- **Data Durability**: Data must be durably preserved to allow historical analysis.

### Required Components:

- **Cluster Database CP** (e.g., MongoDB for strong consistency) for storing and managing long-term statistical data. A CP database system will ensure that all reads return the most recent version of the data.
- **Relational Database Management System** (if opting for SQL databases) or Data Warehouse solutions for storing and analyzing large volumes of data.

## Question 2: Alerts

### Business Constraint for Architecture:

- **Real-Time Availability**: The system must be capable of detecting and notifying potential threats without delay, requiring high availability.
- **Responsiveness**: Alert processing must be fast to enable immediate intervention in the case of detecting a high-risk individual.
- **Reliability**: False alerts must be minimized to maintain system credibility.

### Component to Choose:

- **Kafka Stream Cluster** for real-time data stream management and processing. Kafka is well-suited for this role due to its ability to handle large volumes of real-time data with high availability.

By combining these elements, your architecture will be able to meet both technical and business requirements for alert services and long-term statistical analysis.

## Thank You for Your Attention!