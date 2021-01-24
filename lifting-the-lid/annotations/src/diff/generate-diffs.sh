#!/usr/bin/env bash



echo -e "\n00/01\n"

diff -uN 00-empty/ 01-initial/

echo -e "\n01/02\n"
diff -uN 01-initial/ 02-reflection-invoke/


echo -e "\n02/03\n"
diff -uN 02-reflection-invoke/ 03-test-annotation/


echo -e "\n03/04\n"
diff -uN 03-test-annotation/ 04-tidy-logging/


echo -e "\n04/05\n"
diff -uN 04-tidy-logging/ 05-extract-methods/


echo -e "\n05/06\n"
diff -uN 05-extract-methods/ 06-remove-repetition/


echo "add \"new file mode 100644\" for new files after diff generated and remove prefix"