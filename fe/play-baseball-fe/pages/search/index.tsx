import { useRouter } from 'next/router';
import Link from "next/link"
import TopBar from "../../components/TopBar"

const Search: React.FC = () => {
  const handleSearch = (searchTerm: string) => {
    console.log('Searching for:', searchTerm);
    // Add your search logic here
  };

    return <div>
      <TopBar onSearch={handleSearch} ></TopBar>
    </div>
  }

export default Search;